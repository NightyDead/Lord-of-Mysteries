package com.nightydead.lordofmysteries.ritual;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * 神秘学仪式管理器
 * 负责检查玩家在晋升高序列时是否满足对应的仪式条件
 * <p>
 * 序列 5 及以上（数字 >= 5）：低序列阶段，无需仪式即可晋升
 * 序列 4 及以下（半神阶段）：必须满足特定的仪式条件才能晋升
 * <p>
 * 每个序列的仪式由独立方法实现，支持按途径区分不同的仪式内容
 */
public class MysticalRitualManager {

    /**
     * 检查玩家是否满足晋升目标序列的仪式条件
     *
     * @param player    待晋升的玩家
     * @param data      玩家的非凡数据
     * @param pathway   目标途径 ID
     * @param targetSeq 目标序列号
     * @return 满足仪式条件或无需仪式时返回 true，否则返回 false
     */
    public static boolean checkAndConsumeRitual(Player player, PlayerData data, String pathway, int targetSeq) {
        // 序列 5 及以上（低序列阶段）不需要仪式
        if (targetSeq >= 5) {
            return true;
        }

        // 序列 4 及以下（半神阶段）必须满足仪式条件
        return switch (targetSeq) {
            case 4 -> checkSequence4Ritual(player, pathway);
            case 3 -> checkSequence3Ritual(player, pathway);
            case 2 -> checkSequence2Ritual(player, pathway);
            case 1 -> checkSequence1Ritual(player, pathway);
            case 0 -> checkSequence0Ritual(player, pathway);
            default -> false;
        };
    }

    /**
     * 序列 4 仪式检查（半神入门）
     * 不同途径有不同的仪式要求：
     * - 占卜家途径（fool）：周围 16 格内至少需要 15 个活体生物作为"观众"
     * - 偷盗者途径（marauder/error）：预留，待实现
     *
     * @param player  待晋升的玩家
     * @param pathway 目标途径 ID
     * @return 仪式是否满足
     */
    private static boolean checkSequence4Ritual(Player player, String pathway) {
        if (player.level().isClientSide) return true;

        switch (pathway.toLowerCase()) {
            case "fool", "seer" -> { // 占卜家途径序列 4「诡法师」
                // 仪式要求：在至少 15 个活体生物的"注视"下完成晋升
                String pathKey = "pathway." + LordofMysteries.MODID + "." + pathway.toLowerCase();
                player.sendSystemMessage(Component.translatable("message.lordofmysteries.ritual.checking", Component.translatable(pathKey), 4));

                // 扫描玩家周围半径 16 格、高度 8 格范围内的所有活体生物
                AABB scanArea = player.getBoundingBox().inflate(16.0D, 8.0D, 16.0D);
                List<LivingEntity> spectators = player.level().getEntitiesOfClass(LivingEntity.class, scanArea,
                        entity -> entity != player && entity.isAlive());

                // 核心判定：需要 15 个及以上的"观众"
                boolean isRitualReady = spectators.size() >= 15;

                if (!isRitualReady) {
                    player.sendSystemMessage(Component.translatable("message.lordofmysteries.ritual.failed.bizarro_sorcerer", spectators.size()));
                    return false;
                }
                return true;
            }
            case "marauder", "error" -> { // 偷盗者途径序列 4「寄生者」
                // 预留：可在后续接入雷暴天气判定等逻辑
                return true;
            }
        }
        return true;
    }

    /** 序列 3 仪式检查（预留，待实现） */
    private static boolean checkSequence3Ritual(Player player, String pathway) { return true; }
    /** 序列 2 仪式检查（预留，待实现） */
    private static boolean checkSequence2Ritual(Player player, String pathway) { return true; }
    /** 序列 1 仪式检查（预留，待实现） */
    private static boolean checkSequence1Ritual(Player player, String pathway) { return true; }
    /** 序列 0 仪式检查（预留，待实现） */
    private static boolean checkSequence0Ritual(Player player, String pathway) { return true; }
}
