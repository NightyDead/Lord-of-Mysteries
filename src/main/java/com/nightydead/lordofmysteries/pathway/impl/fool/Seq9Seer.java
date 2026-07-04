package com.nightydead.lordofmysteries.pathway.impl.fool;

import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.data.PlayerData;
import com.nightydead.lordofmysteries.pathway.abstracts.ISequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * 占卜家（Fool）途径 - 序列 9「占卜家」
 * <p>
 * 晋升效果：灵性上限提升至 100
 * 被动能力：在夜间或身处地底/洞穴/室内时自动获得永久夜视效果
 * 失去效果：灵性上限回退至 20（凡人水平）
 */
public class Seq9Seer implements ISequence {

    @Override
    public int getSequenceNumber() { return 9; }

    @Override
    public String getTranslationKey() { return "sequence.lordofmysteries.fool.9"; }

    /**
     * 晋升时触发：将灵性上限和当前灵性设置为 100
     */
    @Override
    public void onAbsorbed(Player player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        data.setMaxSpirituality(100);
        data.setSpirituality(100);
    }

    /**
     * 失去序列时触发：将灵性上限回退至 20，超出部分截断
     */
    @Override
    public void onRemoved(Player player) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        data.setMaxSpirituality(20);
        if (data.getSpirituality() > 20) {
            data.setSpirituality(20);
        }
    }

    /**
     * 每 tick 被动逻辑（每 20 tick / 1 秒检测一次）：
     * - 夜间或身处地底/室内 → 赋予永久夜视（隐藏粒子和图标）
     * - 白天且处于露天环境 → 移除夜视效果
     */
    @Override
    public void tick(Player player) {
        if (player.level().isClientSide()) return;

        // 每 20 tick（1秒）检查一次环境状态
        if (player.tickCount % 20 == 0) {
            net.minecraft.world.level.Level level = player.level();
            net.minecraft.core.BlockPos pos = player.blockPosition();

            // 判定 1：当前是否为夜晚
            boolean isNight = level.isNight();

            // 判定 2：玩家头顶是否被遮挡（处于洞穴/室内）
            boolean isInCaveOrIndoor = !level.canSeeSky(pos);

            // 夜间或身处地底/室内时，占卜家的灵感复苏
            if (isNight || isInCaveOrIndoor) {
                if (!player.hasEffect(MobEffects.NIGHT_VISION)) {
                    // 赋予永久夜视（duration=-1），隐藏粒子和图标
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 0, false, false, false));
                }
            } else {
                // 白天且露天时移除夜视
                if (player.hasEffect(MobEffects.NIGHT_VISION)) {
                    MobEffectInstance effect = player.getEffect(MobEffects.NIGHT_VISION);
                    if (effect != null && effect.getDuration() <= 0) {
                        player.removeEffect(MobEffects.NIGHT_VISION);
                    }
                }
            }
        }
    }
}
