package com.nightydead.lordofmysteries.event;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.data.PlayerData;
import com.nightydead.lordofmysteries.item.custom.ModPotionItem;
import com.nightydead.lordofmysteries.network.*;
import com.nightydead.lordofmysteries.ritual.MysticalRitualManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * 神秘学核心机制处理器
 * 负责处理玩家吸收非凡特性/魔药的完整晋升、跨途径、堆叠与疯狂判定
 */
@EventBusSubscriber(modid = LordofMysteries.MODID)
public class ModMysticalMechanics {

    /**
     * 玩家使用物品完成事件 - 处理非凡特性/魔药的吸收逻辑
     * 区分聚合特性物品和单体特性/魔药物品两种吸收场景
     *
     * @param event 物品使用完成事件
     */
    @SubscribeEvent
    public static void onAbsorbCharacteristic(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) return;

        ItemStack stack = event.getItem();
        var data = player.getData(ModAttachments.PLAYER_DATA.get());

        // 1. 处理"聚合特性"物品（死亡后析出的复合特性）
        if (stack.has(ModDataComponents.AGGREGATED_FEATURES.get())) {
            List<String> features = stack.get(ModDataComponents.AGGREGATED_FEATURES.get());
            if (features != null && !features.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.lordofmysteries.absorption.start"));
                for (String featureStr : features) {
                    try {
                        String[] split = featureStr.split(":");
                        String pathway = split[0];
                        int seq = Integer.parseInt(split[1]);
                        executeAbsorptionLogic(player, data, pathway, seq, calculateDefaultMaxSp(seq), false);
                    } catch (Exception e) {
                        System.out.println("解析聚合特性失败: " + featureStr);
                    }
                }
            }
            return;
        }

        // 2. 处理单体非凡特性/魔药物品
        if (!stack.has(ModDataComponents.PATHWAY.get()) || !stack.has(ModDataComponents.SEQUENCE.get())) return;

        String itemPathway = stack.get(ModDataComponents.PATHWAY.get());
        Integer itemSequence = stack.get(ModDataComponents.SEQUENCE.get());
        if (itemPathway == null || itemSequence == null) return;

        // 提取动态灵性上限逻辑
        int maxSp = stack.has(ModDataComponents.MAX_SPIRITUALITY.get()) ?
        stack.get(ModDataComponents.MAX_SPIRITUALITY.get()) : calculateDefaultMaxSp(itemSequence);
        boolean isPotion = stack.getItem() instanceof ModPotionItem;

        executeAbsorptionLogic(player, data, itemPathway, itemSequence, maxSp, isPotion);
    }

    /**
     * 执行吸收逻辑的核心方法
     * 根据玩家当前状态判断是凡人首次吸收还是非凡者继续吸收
     *
     * @param player  吸收玩家
     * @param data    玩家非凡数据
     * @param pathway 吸收的途径 ID
     * @param seq     吸收的序列号
     * @param maxSp   灵性上限值
     * @param isPotion 是否为魔药（而非原始特性）
     */
    private static void executeAbsorptionLogic(Player player, PlayerData data, String pathway, int seq, int maxSp, boolean isPotion) {
        int ticksBefore = data.getSdcTicks();

        // 判断是否为凡人首次走上超凡道路
        boolean isNormalHuman = "none".equals(data.getCurrentPathway()) || data.getCurrentSequence() >= 10;
        if (isNormalHuman) {
            handleNormalHumanAbsorption(player, data, pathway, seq, maxSp, isPotion);
        } else {
            handleBeyonderAbsorption(player, data, pathway, seq, maxSp, isPotion);
        }

        // 扣除理智值
        data.addSanity(-calculateSanityPenalty(seq));

        if (player instanceof ServerPlayer serverPlayer) {
            syncAllData(serverPlayer, data);
        }

        // 理智崩溃判定
        if (data.getSanity() <= 0) {
            if (ticksBefore == -1 && data.getSdcTicks() != -1) {
                player.sendSystemMessage(Component.translatable("message.lordofmysteries.sanity.collapse.irreversible"));
            } else if (ticksBefore == -1) {
                player.sendSystemMessage(Component.translatable("message.lordofmysteries.sanity.collapse.immediate"));
                handleContaminationAndMadness(player, data, pathway, seq);
            }
        }
    }

    /**
     * 处理凡人首次吸收的逻辑
     * 检查仪式条件、计算成功概率、写入超凡状态
     */
    private static void handleNormalHumanAbsorption(Player player, PlayerData data, String pathway, int seq, int maxSp, boolean isPotion) {
        if (!MysticalRitualManager.checkAndConsumeRitual(player, data, pathway, seq)) {
            String pathKey = "pathway." + LordofMysteries.MODID + "." + pathway.toLowerCase();
            player.sendSystemMessage(Component.translatable("message.lordofmysteries.rejection.mortal", Component.translatable(pathKey)));
            handleContaminationAndMadness(player, data, pathway, seq);
            return;
        }

        double chance = isPotion ? calculatePotionSuccessChance(seq) : calculateRawCharacteristicChance(seq);

        if (player.level().random.nextDouble() < chance) {
            String pathKey = "pathway." + LordofMysteries.MODID + "." + pathway.toLowerCase();
            player.sendSystemMessage(Component.translatable("message.lordofmysteries.potion.consumed", Component.translatable(pathKey), seq));

            // 写入超凡状态，顺便把灵性充满
            data.setCurrentPathway(pathway);
            data.setCurrentSequence(seq);
            data.addAbsorbedRecord(pathway, seq);
            data.setMaxSpirituality(maxSp);
            data.setSpirituality(maxSp);
            data.setDigestion(0.0F);
        } else {
            handleContaminationAndMadness(player, data, pathway, seq);
        }
    }

    /**
     * 处理非凡者继续吸收的逻辑
     * 区分三种场景：跨途径吸收、同途径晋升、同序列堆叠
     */
    private static void handleBeyonderAbsorption(Player player, PlayerData data, String pathway, int seq, int maxSp, boolean isPotion) {
        // 场景 1：跨途径吸收
        if (!data.getCurrentPathway().equals(pathway)) {
            if (isPotion && player.level().random.nextDouble() < 0.005) {
                player.sendSystemMessage(Component.translatable("message.lordofmysteries.cross_pathway.miracle"));
                data.setCurrentPathway(pathway);
                data.setCurrentSequence(seq);
                data.addAbsorbedRecord(pathway, seq);
            } else {
                handleContaminationAndMadness(player, data, pathway, seq);
            }
            return;
        }

        // 场景 2：同途径晋升下一序列
        if (data.getCurrentSequence() - 1 == seq) {
            if (!MysticalRitualManager.checkAndConsumeRitual(player, data, pathway, seq)) {
                player.sendSystemMessage(Component.translatable("message.lordofmysteries.rejection.beyonder", seq));
                handleContaminationAndMadness(player, data, pathway, seq);
                return;
            }

            double finalChance = (isPotion && data.getDigestion() >= 1.0F) ? 1.0 :
            (isPotion ? calculatePotionSuccessChance(seq) : calculateRawCharacteristicChance(seq)) * (data.getDigestion() + 0.1);

            if (player.level().random.nextDouble() < finalChance) {
                data.setCurrentSequence(seq);
                data.addAbsorbedRecord(pathway, seq);
                data.setMaxSpirituality(maxSp);
                data.setSpirituality(maxSp);
                data.setDigestion(0.0F);
                player.sendSystemMessage(Component.translatable("message.lordofmysteries.upgrade.success", seq));
            } else {
                handleContaminationAndMadness(player, data, pathway, seq);
            }
        }
        // 场景 3：同序列堆叠
        else if (seq >= data.getCurrentSequence()) {
            if (data.getDigestion() >= 1.0F && player.level().random.nextDouble() < (isPotion ? 0.40 : 0.10)) {
                data.addAbsorbedRecord(pathway, seq);
                player.sendSystemMessage(Component.translatable("message.lordofmysteries.characteristic.stack", seq));
            } else {
                handleContaminationAndMadness(player, data, pathway, seq);
            }
        } else {
            handleContaminationAndMadness(player, data, pathway, seq);
        }
    }

    /**
     * 处理污染与失控逻辑
     * 赋予负面效果、启动失控倒计时
     */
    private static void handleContaminationAndMadness(Player player, PlayerData data, String pathway, int sequence) {
        player.sendSystemMessage(Component.translatable("message.lordofmysteries.contamination.loss_of_control"));
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 300, 0));
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0));
        data.addAbsorbedRecord(pathway, sequence);
        data.setSdcTicks(300); // 15 秒失控倒计时
    }

    /** 根据序列号计算默认灵性上限：序列越低（等级越高）灵性上限越高 */
    private static int calculateDefaultMaxSp(int seq) {
        return (10 - seq) * 50;
    }

    /** 根据序列号计算理智惩罚值：序列越低惩罚越重 */
    private static int calculateSanityPenalty(int seq) {
        return switch (seq) {
            case 9, 8, 7 -> 40;
            case 6, 5, 4 -> 80;
            case 3, 2, 1 -> 150;
            default -> 500;
        };
    }

    /** 根据序列号计算魔药吸收成功率：序列越低成功率越低 */
    private static double calculatePotionSuccessChance(int seq) {
        return switch (seq) {
            case 9 -> 1.0;
            case 8 -> 0.95;
            case 7 -> 0.90;
            case 6 -> 0.75;
            case 5 -> 0.60;
            default -> 0.01;
        };
    }

    /** 根据序列号计算原始特性吸收成功率：极低，远低于魔药 */
    private static double calculateRawCharacteristicChance(int seq) {
        return switch (seq) {
            case 9 -> 0.20;
            case 8 -> 0.01;
            case 7 -> 0.0005;
            case 6, 5 -> 0.00001;
            default -> 0.0;
        };
    }

    /**
     * 同步所有神秘学数据到客户端
     * 发送理智、灵性、消化度、途径/序列四个数据包
     *
     * @param player 目标玩家
     * @param data   玩家非凡数据
     */
    public static void syncAllData(ServerPlayer player, PlayerData data) {
        PacketDistributor.sendToPlayer(player, new SyncSanityPacket(data.getSanity()));
        PacketDistributor.sendToPlayer(player, new SyncSpiritualityPacket(data.getSpirituality(), data.getMaxSpiritual()));
        PacketDistributor.sendToPlayer(player, new SyncDigestionPacket(data.getDigestion()));
        PacketDistributor.sendToPlayer(player, new SyncPathwayPacket(data.getCurrentPathway(), data.getCurrentSequence()));
    }
}