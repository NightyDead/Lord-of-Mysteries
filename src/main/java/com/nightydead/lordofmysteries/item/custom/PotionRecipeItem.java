package com.nightydead.lordofmysteries.item.custom;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.LearnedRecipesData;
import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.data.PotionRecipeData;
import com.nightydead.lordofmysteries.network.SyncLearnedRecipesPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * 魔药配方物品类
 * 记载特定途径序列的魔药配方信息，右键使用可学习配方（打印到控制台）
 */
public class PotionRecipeItem extends Item {

    public PotionRecipeItem() {
        this(createDefaultProperties());
    }

    public PotionRecipeItem(Properties properties) {
        super(properties);
    }

    public static Properties createDefaultProperties() {
        return new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON);
    }

    /**
     * 右键使用：学习魔药配方到玩家知识面板
     * 已学过的配方不消耗物品，未学过的写入 Capability 并消耗
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        PotionRecipeData data = stack.get(ModDataComponents.RECIPE_DATA.get());

        if (data != null) {
            if (!level.isClientSide) {
                LearnedRecipesData learned = player.getData(ModAttachments.LEARNED_RECIPES.get());

                if (learned.hasLearned(data.pathway(), data.sequence())) {
                    // 重复：不消耗，给提示
                    player.displayClientMessage(
                            Component.translatable("message.lordofmysteries.recipe.already_known").withStyle(ChatFormatting.GRAY),
                            true
                    );
                    return InteractionResultHolder.pass(stack);
                }

                // 新学习：写入 Capability、消耗物品
                learned.learn(data.pathway(), data.sequence());
                player.setData(ModAttachments.LEARNED_RECIPES.get(), learned);

                if (!player.isCreative()) {
                    stack.shrink(1);
                }

                String pathKey = "pathway." + LordofMysteries.MODID + "." + data.pathway();
                String seqKey = sequenceKey(data.pathway(), data.sequence());
                player.displayClientMessage(
                        Component.translatable("message.lordofmysteries.recipe.learned",
                                Component.translatable(pathKey),
                                data.sequence(),
                                Component.translatable(seqKey)
                        ).withStyle(ChatFormatting.GREEN),
                        true
                );

                LordofMysteries.LOGGER.info("Player {} learned {}:{}",
                        player.getName().getString(), data.pathway(), data.sequence());

                // 同步已学配方到客户端
                if (player instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(serverPlayer,
                            new SyncLearnedRecipesPacket(learned.getAll()));
                }

                return InteractionResultHolder.success(stack);
            }
            // 客户端直接返回成功（服务端逻辑完成后会同步状态）
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    /** 配方纸显示附魔光芒效果 */
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    /**
     * 添加物品悬停提示文本
     * 显示途径、序列、主材、辅材和获取方式
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        PotionRecipeData data = stack.get(ModDataComponents.RECIPE_DATA.get());

        if (data != null) {
            String pathKey = "pathway." + LordofMysteries.MODID + "." + data.pathway().toLowerCase();
            String seqKey = sequenceKey(data.pathway(), data.sequence());
            tooltipComponents.add(Component.translatable("tooltip.lordofmysteries.recipe.title").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD));
            tooltipComponents.add(Component.translatable("tooltip.lordofmysteries.recipe.pathway",
                    Component.translatable(pathKey)).withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(Component.translatable("tooltip.lordofmysteries.recipe.sequence",
                    data.sequence(),
                    Component.translatable(seqKey)
            ).withStyle(ChatFormatting.DARK_RED));

            // 主材列表
            List<String> mainMats = data.mainMaterials();
            if (mainMats != null && !mainMats.isEmpty()) {
                tooltipComponents.add(Component.translatable("tooltip.lordofmysteries.recipe.main_materials_title").withStyle(ChatFormatting.AQUA));
                for (String mat : mainMats) {
                    tooltipComponents.add(Component.literal("  • ").append(translateMaterialComponent(mat)).withStyle(ChatFormatting.GRAY));
                }
            }

            // 辅材列表
            List<String> auxMats = data.auxiliaryMaterials();
            if (auxMats != null && !auxMats.isEmpty()) {
                tooltipComponents.add(Component.translatable("tooltip.lordofmysteries.recipe.aux_materials_title").withStyle(ChatFormatting.AQUA));
                for (String mat : auxMats) {
                    tooltipComponents.add(Component.literal("  • ").append(translateMaterialComponent(mat)).withStyle(ChatFormatting.GRAY));
                }
            }

            // 获取方式
            String acq = data.acquisition();
            if (acq != null && !acq.isEmpty()) {
                tooltipComponents.add(Component.translatable("tooltip.lordofmysteries.recipe.acquisition_title").withStyle(ChatFormatting.LIGHT_PURPLE));
                tooltipComponents.add(Component.literal("  ").append(Component.translatable(acq)).withStyle(ChatFormatting.GRAY));
            }
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    /** 构建序列翻译键，格式: sequence.lordofmysteries.<pathway>.<seq> */
    private static String sequenceKey(String pathway, int sequence) {
        return "sequence." + LordofMysteries.MODID + "." + pathway.toLowerCase() + "." + sequence;
    }

    /** 将材料 ID 翻译为可翻译组件，依次尝试主材、辅材、纯水翻译键，失败则回退为原文 */
    private static Component translateMaterialComponent(String id) {
        String modid = LordofMysteries.MODID;
        // 尝试主材翻译键
        String key = "item." + modid + ".main_material." + id;
        Component result = Component.translatable(key);
        if (!result.getString().equals(key)) {
            return result;
        }
        // 尝试辅材翻译键
        key = "item." + modid + ".auxiliary_material." + id;
        result = Component.translatable(key);
        if (!result.getString().equals(key)) {
            return result;
        }
        // 纯水特殊处理
        if (id.equals("pure_water")) {
            key = "item." + modid + ".pure_water.pure_water.effect.empty";
            result = Component.translatable(key);
            if (!result.getString().equals(key)) {
                return result;
            }
        }
        // 无翻译则回退原文
        return Component.literal(id);
    }
}
