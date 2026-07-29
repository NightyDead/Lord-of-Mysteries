package com.nightydead.lordofmysteries.event;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.data.PlayerData;
import com.nightydead.lordofmysteries.pathway.PathwayRegistry;
import com.nightydead.lordofmysteries.pathway.abstracts.AbstractPathway;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 诡秘之主调试与管理指令
 * <p>
 * 提供管理员专用的玩家非凡属性操控指令，用于测试、调试与快速恢复。
 * 权限要求：OP 2级
 */
@EventBusSubscriber(modid = LordofMysteries.MODID)
public class ModCommands {

    /** 途径名自动补全：从 PathwayRegistry 动态获取已注册的途径 ID */
    private static final SuggestionProvider<CommandSourceStack> PATHWAY_SUGGESTIONS =
            (ctx, builder) -> {
                for (AbstractPathway pw : PathwayRegistry.getAllPathways()) {
                    String id = pw.getRegistryName().getPath();
                    builder.suggest(id);
                }
                return builder.buildFuture();
            };

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("lom")
                        .requires(src -> src.hasPermission(2))
                        // ==================== /lom set pathway <player> <pathway> <sequence> ====================
                        .then(Commands.literal("set")
                                .then(Commands.literal("pathway")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("pathway", StringArgumentType.word())
                                                        .suggests(PATHWAY_SUGGESTIONS)
                                                        .then(Commands.argument("sequence", IntegerArgumentType.integer(0, 9))
                                                                .executes(ModCommands::setPathway)
                                                        ))))
                                // /lom set sanity <player> <value>
                                .then(Commands.literal("sanity")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("value", IntegerArgumentType.integer(0, 100))
                                                        .executes(ModCommands::setSanity)
                                                )))
                                // /lom set spirituality <player> <value>
                                .then(Commands.literal("spirituality")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                        .executes(ModCommands::setSpirituality)
                                                )))
                                // /lom set digestion <player> <value>
                                .then(Commands.literal("digestion")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                        .executes(ModCommands::setDigestion)
                                                )))
                        )
                        // ==================== /lom clear <player> ====================
                        .then(Commands.literal("clear")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ModCommands::clearPlayer)
                                ))
                        // ==================== /lom info [player] ====================
                        .then(Commands.literal("info")
                                .executes(ctx -> showInfo(ctx, ctx.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> showInfo(ctx, EntityArgument.getPlayer(ctx, "player")))
                                ))
        );
    }

    // ==================== 命令执行逻辑 ====================

    /** /lom set pathway <player> <pathway> <sequence> */
    private static int setPathway(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        String pathway = StringArgumentType.getString(ctx, "pathway").toLowerCase();
        int sequence = IntegerArgumentType.getInteger(ctx, "sequence");

        // 验证途径存在
        if (PathwayRegistry.get(pathway) == null) {
            ctx.getSource().sendFailure(Component.literal("§c未知途径: " + pathway));
            return 0;
        }

        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());

        // 若已有旧途径，先触发移除回调
        if (!"none".equals(data.getCurrentPathway()) && data.getCurrentSequence() < 10) {
            ModMysticalMechanics.invokeOnRemoved(player, data.getCurrentPathway(), data.getCurrentSequence());
        }

        // 填充非凡特性历史：从序列 9 到目标序列（含），确保死亡时掉落完整聚合特性
        List<String> records = new ArrayList<>();
        for (int seq = 9; seq >= sequence; seq--) {
            records.add(pathway + ":" + seq);
        }
        data.setAbsorbedCharacteristics(records);

        // 写入新身份
        data.setCurrentPathway(pathway);
        data.setCurrentSequence(sequence);
        data.setDigestion(0);
        data.setSameSeqStackCount(0);

        // 触发目标序列的 onAbsorbed 回调（设置灵性上限等）
        ModMysticalMechanics.invokeOnAbsorbed(player, pathway, sequence);

        // 若目标序列无实现类，使用 fallback 灵性上限
        if (PathwayRegistry.get(pathway).getSequence(sequence) == null) {
            int fallbackSpirit = getFallbackSpirituality(sequence);
            data.setMaxSpirituality(fallbackSpirit);
        }

        // 灵性充满至新上限
        data.setSpirituality(data.getMaxSpiritual());
        // 重置理智上限为 100（指令强制定制，不受堆叠影响）
        data.setMaxSanity(100);
        data.setSanity(100);

        // 自动学习目标序列的魔药配方
        ModMysticalMechanics.autoLearnRecipe(player, pathway, sequence);

        // 同步到客户端
        ModMysticalMechanics.syncAllData(player, data);

        String pathKey = "pathway." + LordofMysteries.MODID + "." + pathway;
        String seqKey = "sequence." + LordofMysteries.MODID + "." + pathway + "." + sequence;
        String pathName = Component.translatable(pathKey).getString();
        String seqName = Component.translatable(seqKey).getString();

        ctx.getSource().sendSuccess(() -> Component.literal(
                "§a已将 " + player.getName().getString() + " 设为 " + pathName
                        + "途径 · 序列" + sequence + " " + seqName
                        + "（非凡特性记录: " + records.size() + " 条）"), true);
        return 1;
    }

    /** /lom set sanity <player> <value> */
    private static int setSanity(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        int value = IntegerArgumentType.getInteger(ctx, "value");

        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        data.setSanity(value);
        ModMysticalMechanics.syncAllData(player, data);

        ctx.getSource().sendSuccess(() -> Component.literal(
                "§a已将 " + player.getName().getString() + " 的理智设为 " + value), true);
        return 1;
    }

    /** /lom set spirituality <player> <value> */
    private static int setSpirituality(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        int value = IntegerArgumentType.getInteger(ctx, "value");

        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        // setSpirituality 内部有限幅，这里先放宽上限再设值
        if (value > data.getMaxSpiritual()) {
            data.setMaxSpirituality(value);
        }
        data.setSpirituality(value);
        ModMysticalMechanics.syncAllData(player, data);

        ctx.getSource().sendSuccess(() -> Component.literal(
                "§a已将 " + player.getName().getString() + " 的灵性设为 " + value
                        + "（上限: " + data.getMaxSpiritual() + "）"), true);
        return 1;
    }

    /** /lom set digestion <player> <value> */
    private static int setDigestion(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        int value = IntegerArgumentType.getInteger(ctx, "value");

        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        data.setDigestion(value);
        ModMysticalMechanics.syncAllData(player, data);

        ctx.getSource().sendSuccess(() -> Component.literal(
                "§a已将 " + player.getName().getString() + " 的消化度设为 " + value
                        + "（上限: " + data.getEffectiveMaxDigestion() + "）"), true);
        return 1;
    }

    /** /lom clear <player> */
    private static int clearPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());

        // 先触发旧序列的移除回调
        if (!"none".equals(data.getCurrentPathway()) && data.getCurrentSequence() < 10) {
            ModMysticalMechanics.invokeOnRemoved(player, data.getCurrentPathway(), data.getCurrentSequence());
        }
        data.reset();
        ModMysticalMechanics.syncAllData(player, data);

        ctx.getSource().sendSuccess(() -> Component.literal(
                "§e已将 " + player.getName().getString() + " 重置为凡人"), true);
        return 1;
    }

    /** /lom info [player] — 打印当前非凡状态摘要 */
    private static int showInfo(CommandContext<CommandSourceStack> ctx, ServerPlayer player) throws CommandSyntaxException {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        String pathway = data.getCurrentPathway();
        int seq = data.getCurrentSequence();
        boolean isMortal = "none".equals(pathway) || seq >= 10;

        StringBuilder sb = new StringBuilder();
        sb.append("\n§6===== ").append(player.getName().getString()).append(" 非凡状态 =====");

        if (isMortal) {
            sb.append("\n§7身份: 凡人");
        } else {
            String pathKey = "pathway." + LordofMysteries.MODID + "." + pathway;
            String seqKey = "sequence." + LordofMysteries.MODID + "." + pathway + "." + seq;
            sb.append("\n§e身份: ").append(Component.translatable(pathKey).getString())
                    .append("途径 · 序列").append(seq).append(" ")
                    .append(Component.translatable(seqKey).getString());
        }

        sb.append("\n§c理智: ").append(data.getSanity()).append(" / ").append(data.getMaxSanity());
        sb.append("\n§b灵性: ").append(data.getSpirituality()).append(" / ").append(data.getMaxSpiritual());
        sb.append("\n§a消化度: ").append(data.getDigestion()).append(" / ").append(data.getEffectiveMaxDigestion());
        sb.append(" (").append(String.format("%.1f%%", data.getDigestionRatio() * 100)).append(")");

        var records = data.getAbsorbedCharacteristics();
        sb.append("\n§d已吸收非凡特性: ").append(records != null ? records.size() : 0).append(" 条");
        if (records != null && !records.isEmpty()) {
            sb.append("\n§7  ").append(String.join(", ", records));
        }

        sb.append("\n§8堆叠: ").append(data.getSameSeqStackCount()).append("次 (共")
                .append(data.getTotalCopies()).append("份)");
        sb.append("\n§8失控倒计时: ").append(data.getSdcTicks() == -1 ? "正常" : data.getSdcTicks() + " ticks");

        ctx.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    // ==================== 工具方法 ====================

    /**
     * 当目标序列无 onAbsorbed 实现时，按序列号给一个渐变式灵性上限 fallback
     * 序列9=100 → 序列0=15000
     */
    private static int getFallbackSpirituality(int sequence) {
        return switch (sequence) {
            case 9 -> 100;
            case 8 -> 200;
            case 7 -> 350;
            case 6 -> 500;
            case 5 -> 750;
            case 4 -> 1000;
            case 3 -> 2000;
            case 2 -> 4000;
            case 1 -> 8000;
            case 0 -> 15000;
            default -> 100;
        };
    }
}
