package com.nightydead.lordofmysteries.event;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.LearnedRecipesData;
import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.data.PlayerData;
import com.nightydead.lordofmysteries.item.custom.ModPotionItem;
import com.nightydead.lordofmysteries.network.*;
import com.nightydead.lordofmysteries.pathway.PathwayRegistry;
import com.nightydead.lordofmysteries.pathway.abstracts.AbstractPathway;
import com.nightydead.lordofmysteries.pathway.abstracts.ISequence;
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
                player.displayClientMessage(Component.translatable("message.lordofmysteries.absorption.start"), true);
                for (String featureStr : features) {
                    try {
                        String[] split = featureStr.split(":");
                        String pathway = split[0];
                        int seq = Integer.parseInt(split[1]);
                        executeAbsorptionLogic(player, data, pathway, seq, false);
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

        boolean isPotion = stack.getItem() instanceof ModPotionItem;

        executeAbsorptionLogic(player, data, itemPathway, itemSequence, isPotion);
    }

    /**
     * 执行吸收逻辑的核心方法
     * 根据玩家当前状态判断是凡人首次吸收还是非凡者继续吸收
     *
     * @param player   吸收玩家
     * @param data     玩家非凡数据
     * @param pathway  吸收的途径 ID
     * @param seq      吸收的序列号
     * @param isPotion 是否为魔药（而非原始特性）
     */
    private static void executeAbsorptionLogic(Player player, PlayerData data, String pathway, int seq, boolean isPotion) {
        int ticksBefore = data.getSdcTicks();
        int sanityCost = calculateSanityPenalty(seq);

        // 📚 理智前提检查：吸收会扣除理智，若扣除后理智≤0则直接失控
        if (data.getSanity() - sanityCost <= 0) {
            player.displayClientMessage(Component.translatable("message.lordofmysteries.sanity.insufficient"), true);
            handleContaminationAndMadness(player, data, pathway, seq);
            if (player instanceof ServerPlayer serverPlayer) {
                syncAllData(serverPlayer, data);
            }
            return;
        }

        // 判断是否为凡人首次走上超凡道路
        boolean isNormalHuman = "none".equals(data.getCurrentPathway()) || data.getCurrentSequence() >= 10;
        if (isNormalHuman) {
            handleNormalHumanAbsorption(player, data, pathway, seq, isPotion);
        } else {
            handleBeyonderAbsorption(player, data, pathway, seq, isPotion);
        }

        // 扣除理智值
        data.addSanity(-sanityCost);

        if (player instanceof ServerPlayer serverPlayer) {
            syncAllData(serverPlayer, data);
        }

        // 理智崩溃判定
        if (data.getSanity() <= 0) {
            if (ticksBefore == -1 && data.getSdcTicks() != -1) {
                player.displayClientMessage(Component.translatable("message.lordofmysteries.sanity.collapse.irreversible"), true);
            } else if (ticksBefore == -1) {
                player.displayClientMessage(Component.translatable("message.lordofmysteries.sanity.collapse.immediate"), true);
                handleContaminationAndMadness(player, data, pathway, seq);
            }
        }
    }

    /**
     * 处理凡人首次吸收的逻辑
     * 检查仪式条件、计算成功概率、写入超凡状态
     */
    private static void handleNormalHumanAbsorption(Player player, PlayerData data, String pathway, int seq, boolean isPotion) {
        if (!MysticalRitualManager.checkAndConsumeRitual(player, data, pathway, seq)) {
            String pathKey = "pathway." + LordofMysteries.MODID + "." + pathway.toLowerCase();
            player.displayClientMessage(Component.translatable("message.lordofmysteries.rejection.mortal", Component.translatable(pathKey)), true);
            handleContaminationAndMadness(player, data, pathway, seq);
            return;
        }

        double chance = isPotion ? calculatePotionSuccessChance(seq) : calculateRawCharacteristicChance(seq);

        if (player.level().random.nextDouble() < chance) {
            String pathKey = "pathway." + LordofMysteries.MODID + "." + pathway.toLowerCase();
            player.displayClientMessage(Component.translatable("message.lordofmysteries.potion.consumed", Component.translatable(pathKey), seq), true);

            // 写入超凡状态
            data.setCurrentPathway(pathway);
            data.setCurrentSequence(seq);
            data.addAbsorbedRecord(pathway, seq);
            data.setDigestion(0);
            // 先触发序列专属回调（设置灵性上限等），再将灵性充满至新上限
            invokeOnAbsorbed(player, pathway, seq);
            data.setSpirituality(data.getMaxSpiritual());
            // 自动学习该途径序列的魔药配方
            autoLearnRecipe(player, pathway, seq);
        } else {
            handleContaminationAndMadness(player, data, pathway, seq);
        }
    }

    /**
     * 处理非凡者继续吸收的逻辑
     * 区分三种场景：跨途径吸收、同途径晋升、同序列堆叠
     */
    private static void handleBeyonderAbsorption(Player player, PlayerData data, String pathway, int seq, boolean isPotion) {
        // 场景 1：跨途径吸收
        if (!data.getCurrentPathway().equals(pathway)) {
            if (isPotion && player.level().random.nextDouble() < 0.005) {
                player.displayClientMessage(Component.translatable("message.lordofmysteries.cross_pathway.miracle"), true);
                data.setCurrentPathway(pathway);
                data.setCurrentSequence(seq);
                data.addAbsorbedRecord(pathway, seq);
                data.setDigestion(0);
                // 跨途径奇迹：先触发新序列回调，再将灵性充满
                invokeOnAbsorbed(player, pathway, seq);
                data.setSpirituality(data.getMaxSpiritual());
                // 自动学习该途径序列的魔药配方
                autoLearnRecipe(player, pathway, seq);
            } else {
                handleContaminationAndMadness(player, data, pathway, seq);
            }
            return;
        }

        // 场景 2：同途径晋升下一序列
        if (data.getCurrentSequence() - 1 == seq) {
            if (!MysticalRitualManager.checkAndConsumeRitual(player, data, pathway, seq)) {
                player.displayClientMessage(Component.translatable("message.lordofmysteries.rejection.beyonder", seq), true);
                handleContaminationAndMadness(player, data, pathway, seq);
                return;
            }

            double finalChance = (isPotion && data.getDigestion() >= data.getEffectiveMaxDigestion()) ? 1.0 :
            (isPotion ? calculatePotionSuccessChance(seq) : calculateRawCharacteristicChance(seq)) * (data.getDigestionRatio() + 0.1f);

            if (player.level().random.nextDouble() < finalChance) {
                data.setCurrentSequence(seq);
                data.addAbsorbedRecord(pathway, seq);
                data.setDigestion(0);
                // 先触发序列专属回调（设置灵性上限等），再将灵性充满至新上限
                invokeOnAbsorbed(player, pathway, seq);
                data.setSpirituality(data.getMaxSpiritual());
                player.displayClientMessage(Component.translatable("message.lordofmysteries.upgrade.success", seq), true);
                // 自动学习该途径序列的魔药配方
                autoLearnRecipe(player, pathway, seq);
            } else {
                handleContaminationAndMadness(player, data, pathway, seq);
            }
        }
        // 场景 3：同序列堆叠（已有同途径同序列特性，再次吸收增强）
        else if (seq >= data.getCurrentSequence()) {
            handleSameSequenceStacking(player, data, pathway, seq, isPotion);
        } else {
            handleContaminationAndMadness(player, data, pathway, seq);
        }
    }

    /**
     * 处理同序列堆叠吸收逻辑
     * <p>
     * <b>成功率：</b>1/N（N 为吸收后的总份数）。已有 1 份再喝 = 1/2 = 50%，已有 2 份再喝 = 1/3 ≈ 33%
     * <b>前提：</b>理智扣除后必须 > 0（由 executeAbsorptionLogic 预先校验）
     * <p>
     * <b>成功后属性变更（以单份为基准）：</b>
     * <ul>
     *   <li>灵性上限 +10%/份（2份=110%, 3份=120%...）</li>
     *   <li>能力消耗 -10%/份（2份=90%, 3份=80%...）</li>
     *   <li>能力强度 +10%/份</li>
     *   <li>消化度上限 ×N</li>
     *   <li>理智上限 -20%/份（2份=80, 3份=60, 4份=40...）</li>
     * </ul>
     */
    private static void handleSameSequenceStacking(Player player, PlayerData data, String pathway, int seq, boolean isPotion) {
        int currentStack = data.getSameSeqStackCount();
        int newTotal = currentStack + 2; // N = 已有份数 + 新吸收的 1 份
        double successRate = 1.0 / newTotal;

        if (player.level().random.nextDouble() < successRate) {
            data.addAbsorbedRecord(pathway, seq);
            data.setSameSeqStackCount(currentStack + 1);

            int newCount = currentStack + 1; // 新的堆叠次数

            // 理智上限：100 × (1 - 0.2 × stackCount)
            int newMaxSanity = (int) Math.round(100 * (1 - 0.2 * newCount));
            newMaxSanity = Math.max(1, newMaxSanity); // 最低保留 1 点
            data.setMaxSanity(newMaxSanity);
            data.setSanity(Math.min(data.getSanity(), data.getMaxSanity()));

            // 灵性上限：base × (1 + 0.1 × stackCount)
            float oldSpiritMult = 1.0F + 0.1F * currentStack;
            int baseSpirituality = Math.round(data.getMaxSpiritual() / oldSpiritMult);
            float newSpiritMult = 1.0F + 0.1F * newCount;
            int newMaxSpirituality = Math.round(baseSpirituality * newSpiritMult);
            data.setMaxSpirituality(newMaxSpirituality);
            data.setSpirituality(Math.min(data.getSpirituality(), newMaxSpirituality));

            player.displayClientMessage(Component.translatable("message.lordofmysteries.characteristic.stack", seq), true);
            player.displayClientMessage(Component.literal("§e📚 同序列堆叠×" + (newCount + 1)
                    + " | 灵性上限 " + String.format("%.0f%%", newSpiritMult * 100)
                    + " | 消耗 " + String.format("%.0f%%", (1 - 0.1 * newCount) * 100)
                    + " | 理智上限 " + newMaxSanity), true);
        } else {
            handleContaminationAndMadness(player, data, pathway, seq);
        }
    }

    /**
     * 处理污染与失控逻辑
     * 赋予负面效果、启动失控倒计时
     */
    private static void handleContaminationAndMadness(Player player, PlayerData data, String pathway, int sequence) {
        player.displayClientMessage(Component.translatable("message.lordofmysteries.contamination.loss_of_control"), true);
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 300, 0));
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0));
        data.addAbsorbedRecord(pathway, sequence);
        data.setSdcTicks(300); // 15 秒失控倒计时
    }

    /**
     * 调用序列的 onAbsorbed 回调
     * 用于触发序列专属的晋升逻辑（如设置灵性上限、赋予被动能力等）
     *
     * @param player  吸收成功的玩家
     * @param pathway 途径 ID
     * @param seq     序列号
     */
    public static void invokeOnAbsorbed(Player player, String pathway, int seq) {
        AbstractPathway pathwayObj = PathwayRegistry.get(pathway);
        if (pathwayObj != null) {
            ISequence sequenceObj = pathwayObj.getSequence(seq);
            if (sequenceObj != null) {
                sequenceObj.onAbsorbed(player);
            }
        }
    }

    /**
     * 调用序列的 onRemoved 回调
     * 用于触发序列专属的移除逻辑（如回退灵性上限、剥离被动能力等）
     *
     * @param player  失去序列的玩家
     * @param pathway 途径 ID
     * @param seq     序列号
     */
    public static void invokeOnRemoved(Player player, String pathway, int seq) {
        AbstractPathway pathwayObj = PathwayRegistry.get(pathway);
        if (pathwayObj != null) {
            ISequence sequenceObj = pathwayObj.getSequence(seq);
            if (sequenceObj != null) {
                sequenceObj.onRemoved(player);
            }
        }
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
     * 发送理智、灵性、消化度、途径/序列、灵视状态五个数据包
     *
     * @param player 目标玩家
     * @param data   玩家非凡数据
     */
    public static void syncAllData(ServerPlayer player, PlayerData data) {
        PacketDistributor.sendToPlayer(player, new SyncSanityPacket(data.getSanity()));
        PacketDistributor.sendToPlayer(player, new SyncSpiritualityPacket(data.getSpirituality(), data.getMaxSpiritual()));
        PacketDistributor.sendToPlayer(player, new SyncDigestionPacket(data.getDigestion(), data.getEffectiveMaxDigestion()));
        PacketDistributor.sendToPlayer(player, new SyncPathwayPacket(data.getCurrentPathway(), data.getCurrentSequence()));
        // 👁️ 同步灵视状态，确保客户端登录/定期兑底时灵视 HUD 和实体发光正确渲染
        PacketDistributor.sendToPlayer(player, new SyncVisionPacket(data.isVisionActive()));
    }

    /**
     * 自动学习魔药配方
     * 玩家在成功吸收特性/魔药晋升后，自动习得对应途径序列的配方知识
     *
     * @param player  晋升成功的玩家
     * @param pathway 途径 ID
     * @param seq     序列号
     */
    public static void autoLearnRecipe(Player player, String pathway, int seq) {
        LearnedRecipesData learned = player.getData(ModAttachments.LEARNED_RECIPES.get());
        if (!learned.hasLearned(pathway, seq)) {
            learned.learn(pathway, seq);
            player.setData(ModAttachments.LEARNED_RECIPES.get(), learned);
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new SyncLearnedRecipesPacket(learned.getAll()));
            }
        }
    }
}