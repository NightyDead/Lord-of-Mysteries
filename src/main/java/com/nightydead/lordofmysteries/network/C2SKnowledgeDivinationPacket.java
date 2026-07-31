package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.data.PlayerData;
import com.nightydead.lordofmysteries.data.PotionRecipeData;
import com.nightydead.lordofmysteries.data.PotionRecipeRegistry;
import com.nightydead.lordofmysteries.event.ModMysticalMechanics;
import com.nightydead.lordofmysteries.item.ModItems;
import com.nightydead.lordofmysteries.item.custom.MainMaterialItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 客户端 → 服务端方向的「知识载体占卜」请求数据包
 * <p>
 * 玩家一手持知识载体、另一手持任意魔药主材时，在技能轮盘中选中占卜发送此包，
 * 服务端校验资格与消耗后，将知识载体转化为主材对应的魔药配方纸（主材不消耗）。
 * <p>
 * <b>动态消耗规则</b>（以配方序列与玩家当前序列的差值 n 计）：
 * <ul>
 *   <li>序列相同：基准 50 灵性 + 20 理智</li>
 *   <li>配方低于玩家（n&gt;0）：每低一级消耗 -10%，最低 5 灵性 + 2 理智</li>
 *   <li>配方高于玩家（n&lt;0）：每高一级灵性消耗 +50%、理智消耗 +35% 最大理智，无上限</li>
 * </ul>
 */
public record C2SKnowledgeDivinationPacket() implements CustomPacketPayload {
    /** 数据包唯一标识 ID，注册名为 "lordofmysteries:c2s_knowledge_divination" */
    public static final Type<C2SKnowledgeDivinationPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "c2s_knowledge_divination"));

    /** 基准灵性消耗 */
    private static final int BASE_SPIRITUALITY = 50;
    /** 基准理智消耗 */
    private static final int BASE_SANITY = 20;
    /** 配方低于玩家时，每低一级消耗的减少比例 */
    private static final double LOWER_REDUCTION = 0.1;
    /** 配方低于玩家时的最低灵性消耗 */
    private static final int MIN_SPIRITUALITY = 5;
    /** 配方低于玩家时的最低理智消耗 */
    private static final int MIN_SANITY = 2;
    /** 配方高于玩家时，每高一级灵性消耗的增加比例 */
    private static final double HIGHER_SPIRIT_RATIO = 0.5;
    /** 配方高于玩家时，每高一级理智消耗增加的最大理智比例 */
    private static final double HIGHER_SANITY_RATIO = 0.35;

    /** 无字段数据包，使用 unit 编解码器 */
    public static final StreamCodec<FriendlyByteBuf, C2SKnowledgeDivinationPacket> STREAM_CODEC =
            StreamCodec.unit(new C2SKnowledgeDivinationPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 注册数据包到网络通道（客户端 → 服务端方向）
     *
     * @param registrar 网络注册器
     */
    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(TYPE, STREAM_CODEC, C2SKnowledgeDivinationPacket::handle);
    }

    /**
     * 服务端接收处理：校验资格与手持组合 → 计算动态消耗 → 扣除消耗 → 知识载体转化为配方
     */
    public static void handle(final C2SKnowledgeDivinationPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            player.getExistingData(ModAttachments.PLAYER_DATA.get()).ifPresent(data -> {
                // ==================== 校验：必须是占卜家途径的非凡者 ====================
                if (data.getCurrentSequence() >= 10 || !"fool".equals(data.getCurrentPathway())) {
                    player.displayClientMessage(
                            Component.translatable("message.lordofmysteries.divination.not_seer", "知识载体"), true);
                    return;
                }

                // ==================== 校验：一手知识载体 + 另一手魔药主材 ====================
                ItemStack main = player.getMainHandItem();
                ItemStack off = player.getOffhandItem();
                InteractionHand vesselHand = null;
                ItemStack materialStack = ItemStack.EMPTY;

                if (main.is(ModItems.KNOWLEDGE_VESSEL.get()) && off.getItem() instanceof MainMaterialItem) {
                    vesselHand = InteractionHand.MAIN_HAND;
                    materialStack = off;
                } else if (off.is(ModItems.KNOWLEDGE_VESSEL.get()) && main.getItem() instanceof MainMaterialItem) {
                    vesselHand = InteractionHand.OFF_HAND;
                    materialStack = main;
                }

                if (vesselHand == null || materialStack.isEmpty()) {
                    player.displayClientMessage(
                            Component.translatable("message.lordofmysteries.knowledge_divination.no_combination"), true);
                    return;
                }

                // ==================== 读取主材对应的配方（途径无所谓，只按序列差结算） ====================
                String pathway = materialStack.get(ModDataComponents.PATHWAY.get());
                Integer sequence = materialStack.get(ModDataComponents.SEQUENCE.get());
                if (pathway == null || sequence == null) {
                    player.displayClientMessage(
                            Component.translatable("message.lordofmysteries.knowledge_divination.no_recipe"), true);
                    return;
                }

                PotionRecipeData recipe = PotionRecipeRegistry.get(pathway, sequence);
                if (recipe == null) {
                    recipe = PotionRecipeData.simple(pathway, sequence);
                }

                // ==================== 动态消耗计算 ====================
                int levelDiff = recipe.sequence() - data.getCurrentSequence();
                int spiritCost;
                int sanityCost;
                if (levelDiff > 0) {
                    // 配方低于玩家序列：每低一级消耗 -10%，最低 5 灵性 + 2 理智
                    spiritCost = Math.max(MIN_SPIRITUALITY,
                            (int) Math.round(BASE_SPIRITUALITY * (1 - LOWER_REDUCTION * levelDiff)));
                    sanityCost = Math.max(MIN_SANITY,
                            (int) Math.round(BASE_SANITY * (1 - LOWER_REDUCTION * levelDiff)));
                } else if (levelDiff < 0) {
                    // 配方高于玩家序列：每高一级灵性 +50%、理智 +35% 最大理智，无上限
                    int higher = -levelDiff;
                    spiritCost = (int) Math.round(BASE_SPIRITUALITY * (1 + HIGHER_SPIRIT_RATIO * higher));
                    sanityCost = BASE_SANITY + (int) Math.round(HIGHER_SANITY_RATIO * data.getMaxSanity() * higher);
                } else {
                    spiritCost = BASE_SPIRITUALITY;
                    sanityCost = BASE_SANITY;
                }

                // 灵性消耗应用同序列堆叠倍率（理智不享受折扣，与吸收机制一致）
                int effectiveSpirit = Math.max(1, Math.round(spiritCost * data.getStackCostMultiplier()));

                if (data.getSpirituality() < effectiveSpirit) {
                    player.displayClientMessage(
                            Component.translatable("message.lordofmysteries.divination.no_spirituality", effectiveSpirit), true);
                    return;
                }

                // ==================== 扣除灵性 / 理智 ====================
                data.addSpirituality(-effectiveSpirit);
                data.addSanity(-sanityCost);
                player.setData(ModAttachments.PLAYER_DATA.get(), data);
                PacketDistributor.sendToPlayer(player,
                        new SyncSpiritualityPacket(data.getSpirituality(), data.getMaxSpiritual()));
                PacketDistributor.sendToPlayer(player, new SyncSanityPacket(data.getSanity()));

                // ==================== 知识载体转化为魔药配方（主材不消耗） ====================
                ItemStack vessel = player.getItemInHand(vesselHand);
                ItemStack recipeStack = new ItemStack(ModItems.POTION_RECIPE.get(), 1);
                recipeStack.set(ModDataComponents.RECIPE_DATA.get(), recipe);

                if (vessel.getCount() == 1) {
                    // 仅 1 个：原位替换，直观呈现「知识载体化作配方」
                    player.setItemInHand(vesselHand, recipeStack);
                } else {
                    // 多个：只消耗 1 个，配方放入背包（背包满则掉落）
                    vessel.shrink(1);
                    player.setItemInHand(vesselHand, vessel);
                    if (!player.getInventory().add(recipeStack)) {
                        player.drop(recipeStack, false);
                    }
                }

                // ==================== 成功：增加消化度（与其它占卜一致） ====================
                int gain = PlayerData.getDigestionGain(data.getCurrentSequence());
                data.addDigestion(gain);
                player.setData(ModAttachments.PLAYER_DATA.get(), data);
                PacketDistributor.sendToPlayer(player,
                        new SyncDigestionPacket(data.getDigestion(), data.getEffectiveMaxDigestion()));

                String pathKey = "pathway." + LordofMysteries.MODID + "." + recipe.pathway();
                player.displayClientMessage(
                        Component.translatable("message.lordofmysteries.knowledge_divination.success",
                                Component.translatable(pathKey), recipe.sequence(), effectiveSpirit, sanityCost), true);

                // ==================== 理智归零：即时触发失控（15 秒倒计时） ====================
                if (data.getSanity() <= 0 && data.getSdcTicks() == -1) {
                    ModMysticalMechanics.triggerMadnessOnSanityZero(player, data);
                    player.setData(ModAttachments.PLAYER_DATA.get(), data);
                    ModMysticalMechanics.syncAllData(player, data);
                }
            });
        });
    }
}
