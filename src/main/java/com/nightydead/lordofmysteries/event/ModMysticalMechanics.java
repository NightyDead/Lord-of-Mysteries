package com.nightydead.lordofmysteries.event;

import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.data.PlayerData;
import com.nightydead.lordofmysteries.item.PotionItem;
import com.nightydead.lordofmysteries.network.SyncDigestionPacket;
import com.nightydead.lordofmysteries.network.SyncPathwayPacket;
import com.nightydead.lordofmysteries.network.SyncSanityPacket;
import com.nightydead.lordofmysteries.network.SyncSpiritualityPacket;
import com.nightydead.lordofmysteries.util.MysticalRitualManager;
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
 * 主类：ModMysticalMechanics
 * 已经完美接入【高序列神秘学仪式】扩展口！
 */
@EventBusSubscriber(modid = "lordofmysteries")
public class ModMysticalMechanics {

    @SubscribeEvent
    public static void onAbsorbCharacteristic(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) return;

        ItemStack stack = event.getItem();
        var data = player.getData(ModAttachments.PLAYER_DATA.get());

        // 1. 检查是否是死亡掉落的“聚合特性”物品
        if (stack.has(ModDataComponents.AGGREGATED_FEATURES.get())) {
            List<String> features = stack.get(ModDataComponents.AGGREGATED_FEATURES.get());
            if (features != null && !features.isEmpty()) {
                player.sendSystemMessage(Component.literal("§d[特性容纳] §f你尝试强行吸收一团聚合的生鲜非凡特性..."));
                for (String featureStr : features) {
                    try {
                        String[] split = featureStr.split(":");
                        String pathway = split[0];
                        int seq = Integer.parseInt(split[1]);
                        int maxSp = (10 - seq) * 50;

                        executeAbsorptionLogic(player, data, pathway, seq, maxSp, false);
                    } catch (Exception e) {
                        System.out.println("解析聚合特性失败: " + featureStr);
                    }
                }
                return;
            }
        }

        // 2. 原有的单个非凡物品/魔药吸收逻辑
        if (!stack.has(ModDataComponents.PATHWAY.get()) || !stack.has(ModDataComponents.SEQUENCE.get())) return;

        String itemPathway = stack.get(ModDataComponents.PATHWAY.get());
        Integer itemSequence = stack.get(ModDataComponents.SEQUENCE.get());
        if (itemPathway == null || itemSequence == null) return;

        int maxSp = stack.has(ModDataComponents.MAX_SPIRITUALITY.get()) ? stack.get(ModDataComponents.MAX_SPIRITUALITY.get()) : (10 - itemSequence) * 50;
        boolean isPotion = stack.getItem() instanceof PotionItem;

        executeAbsorptionLogic(player, data, itemPathway, itemSequence, maxSp, isPotion);
    }

    private static void executeAbsorptionLogic(Player player, PlayerData data, String pathway, int seq, int maxSp, boolean isPotion) {
        int ticksBefore = data.getSdcTicks();

        // 1. 执行晋升和数据记录逻辑（此时 data 里的序列、最大灵性、消化度已经重置发生改变）
        boolean isNormalHuman = "none".equals(data.getCurrentPathway()) || data.getCurrentSequence() >= 10;
        if (isNormalHuman) {
            handleNormalHumanAbsorption(player, data, pathway, seq, maxSp, isPotion);
        } else {
            handleBeyonderAbsorption(player, data, pathway, seq, maxSp, isPotion);
        }

        // 2. 理智扣除
        data.addSanity(-calculateSanityPenalty(seq));

        // 🌟【三包齐发】：确保客户端数据瞬间刷新
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncSanityPacket(data.getSanity()));
            PacketDistributor.sendToPlayer(serverPlayer, new SyncSpiritualityPacket(data.getSpirituality(), data.getMaxSpiritual()));
            PacketDistributor.sendToPlayer(serverPlayer, new SyncDigestionPacket(data.getDigestion()));
            PacketDistributor.sendToPlayer(serverPlayer, new SyncPathwayPacket(data.getCurrentPathway(), data.getCurrentSequence()));
        }

        // 3. 理智崩溃判定...
        if (data.getSanity() <= 0) {
            if (ticksBefore == -1 && data.getSdcTicks() != -1) {
                player.sendSystemMessage(Component.literal("§4[理智崩溃] §c残存的理智未能阻挡疯狂，你的失控变得不可逆转..."));
            }
            else if (ticksBefore == -1 && data.getSdcTicks() == -1) {
                player.sendSystemMessage(Component.literal("§4[理智崩溃] §c魔药虽然成功容纳，但你的精神防线已彻底瓦解！你因理智归零而失控了！"));
                handleContaminationAndMadness(player, data, pathway, seq);
            }
        }
    }

    private static void handleNormalHumanAbsorption(Player player, PlayerData data, String pathway, int seq, int maxSp, boolean isPotion) {
        // 🔥【仪式拦截口 1】：凡人如果直接一步登天去吃/喝序列 4 以上的高序列物品
        // 触发仪式管理器判定（虽然凡人一般不可能满足高序列仪式，直接卡死）
        if (!MysticalRitualManager.checkAndConsumeRitual(player, data, pathway, seq)) {
            player.sendSystemMessage(Component.literal("§c[神性排异] §4凡人之躯妄图窃取高位神职，且未举行对应的晋升仪式！"));
            handleContaminationAndMadness(player, data, pathway, seq);
            return;
        }

        double chance = isPotion ? calculatePotionSuccessChance(seq) : calculateRawCharacteristicChance(seq);

        if (player.level().random.nextDouble() < chance) {
            String prefix = isPotion ? "§6[魔药晋升]" : "§d[神迹存活]";
            player.sendSystemMessage(Component.literal(prefix + " §f你成功容纳了 " + pathway + " 途径序列 " + seq + " 的神性！"));
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

    private static void handleBeyonderAbsorption(Player player, PlayerData data, String pathway, int seq, int maxSp, boolean isPotion) {
        if (!data.getCurrentPathway().equals(pathway)) {
            double crossChance = isPotion ? 0.005 : 0.0;
            if (player.level().random.nextDouble() < crossChance) {
                player.sendSystemMessage(Component.literal("§4[异界错乱] §e奇迹发生了！你竟然强行容纳了异途径的魔药..."));
                data.setCurrentPathway(pathway);
                data.setCurrentSequence(seq);
                data.addAbsorbedRecord(pathway, seq);
            } else {
                handleContaminationAndMadness(player, data, pathway, seq);
            }
            return;
        }

        // 正常同途径、邻近序列晋升（高一级晋升，如序列 5 晋升 序列 4）
        if (data.getCurrentSequence() - 1 == seq) {

            // 🔥【核心仪式拦截口 2】：调用仪式管理器。如果检查不通过，直接强制判定为失控！
            if (!MysticalRitualManager.checkAndConsumeRitual(player, data, pathway, seq)) {
                player.sendSystemMessage(Component.literal("§c[神性失衡] §4你没有举行晋升序列 " + seq + " 所需的完整神秘学仪式！神性当场撕裂了你的躯壳！"));
                handleContaminationAndMadness(player, data, pathway, seq);
                return; // 仪式失败，后面的 100% 成功率直接失效，进失控倒计时
            }

            double finalChance;
            if (isPotion && data.getDigestion() >= 1.0F) {
                finalChance = 1.0;
            } else {
                double baseChance = isPotion ? calculatePotionSuccessChance(seq) : calculateRawCharacteristicChance(seq);
                finalChance = baseChance * (data.getDigestion() + 0.1);
            }

            if (player.level().random.nextDouble() < finalChance) {
                data.setCurrentSequence(seq);
                data.addAbsorbedRecord(pathway, seq);
                data.setMaxSpirituality(maxSp);
                data.setSpirituality(maxSp);
                data.setDigestion(0.0F);
                String prefix = isPotion ? "§6[神性的共鸣]" : "§d[粗暴容纳]";
                player.sendSystemMessage(Component.literal(prefix + " §f你成功跨入了新序列 " + seq + "！"));
            } else {
                handleContaminationAndMadness(player, data, pathway, seq);
            }
        }
        // 3. 相同或更低序列特性堆积
        else if (seq >= data.getCurrentSequence()) {
            if (data.getDigestion() < 1.0F) {
                handleContaminationAndMadness(player, data, pathway, seq);
            } else {
                double stackChance = isPotion ? 0.40 : 0.10;
                if (player.level().random.nextDouble() < stackChance) {
                    data.addAbsorbedRecord(pathway, seq);
                    player.sendSystemMessage(Component.literal("§e[特性堆积] §f虽然痛苦，但你扛住了神性超载（序列 " + seq + "）。"));
                } else {
                    handleContaminationAndMadness(player, data, pathway, seq);
                }
            }
        } else {
            handleContaminationAndMadness(player, data, pathway, seq);
        }
    }

    private static void handleContaminationAndMadness(Player player, PlayerData data, String pathway, int sequence) {
        player.sendSystemMessage(Component.literal("§c[不可直视的高维污染] §4你失控了！"));
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 300, 0));
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0));

        data.addAbsorbedRecord(pathway, sequence);
        data.setSdcTicks(300);
    }

    private static int calculateSanityPenalty(int seq) {
        return switch (seq) {
            case 9, 8, 7 -> 40;
            case 6, 5, 4 -> 80;
            case 3, 2, 1 -> 150; // 为高序列追加更重的理智惩罚
            default -> 500;
        };
    }

    private static double calculatePotionSuccessChance(int seq) {
        return switch (seq) {
            case 9 -> 1.0;
            case 8 -> 0.95;
            case 7 -> 0.90;
            case 6 -> 0.75;
            case 5 -> 0.60;
            default -> 0.01; // 半神及以上如果没有仪式，即便喝魔药也只有 1% 的存活率（但上面仪式不通过已被直接 return 拦截）
        };
    }

    private static double calculateRawCharacteristicChance(int seq) {
        return switch (seq) {
            case 9 -> 0.20;
            case 8 -> 0.01;
            case 7 -> 0.0005;
            case 6, 5 -> 0.00001;
            default -> 0.0;
        };
    }
}