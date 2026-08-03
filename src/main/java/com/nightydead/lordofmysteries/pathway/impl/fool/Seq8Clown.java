package com.nightydead.lordofmysteries.pathway.impl.fool;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.data.PlayerData;
import com.nightydead.lordofmysteries.pathway.abstracts.ISequence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * 占卜家（Fool）途径 - 序列 8「小丑」
 * <p>
 * 晋升效果：血量 +2（1颗心）、移动速度 +10%、攻击力 +1、灵性上限提升至 200
 * （序列9占卜家的灵性上限为 100）
 * 失去效果：移除全部属性修饰符并回退血量，灵性上限回退至 100（序列9水平）
 */
public class Seq8Clown implements ISequence {

    /** 血量修饰符 ID：+2 点（1颗心） */
    private static final ResourceLocation HEALTH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "clown_health");
    /** 移速修饰符 ID：+10% */
    private static final ResourceLocation SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "clown_speed");
    /** 攻击力修饰符 ID：+1 */
    private static final ResourceLocation ATTACK_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "clown_attack");

    @Override
    public int getSequenceNumber() { return 8; }

    @Override
    public String getTranslationKey() { return "sequence.lordofmysteries.fool.8"; }

    /**
     * 晋升时触发：增强体质（血量/移速/攻击力）并提升灵性上限至 200
     */
    @Override
    public void onAbsorbed(Player player) {
        // 血量 +2（1颗心）
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health != null && !health.hasModifier(HEALTH_MODIFIER_ID)) {
            health.addPermanentModifier(new AttributeModifier(
                    HEALTH_MODIFIER_ID, 2.0, AttributeModifier.Operation.ADD_VALUE));
        }
        // 移动速度 +10%
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && !speed.hasModifier(SPEED_MODIFIER_ID)) {
            speed.addPermanentModifier(new AttributeModifier(
                    SPEED_MODIFIER_ID, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        // 攻击力 +1
        AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null && !attack.hasModifier(ATTACK_MODIFIER_ID)) {
            attack.addPermanentModifier(new AttributeModifier(
                    ATTACK_MODIFIER_ID, 1.0, AttributeModifier.Operation.ADD_VALUE));
        }
        // 灵性上限提升至 200 并充满
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        data.setMaxSpirituality(200);
        data.setSpirituality(200);
    }

    /**
     * 失去序列时触发：移除全部小丑属性修饰符，灵性上限回退至 100，超出部分截断
     */
    @Override
    public void onRemoved(Player player) {
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(HEALTH_MODIFIER_ID);
        }
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SPEED_MODIFIER_ID);
        }
        AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.removeModifier(ATTACK_MODIFIER_ID);
        }
        // 血量上限回退后，将当前血量裁剪到新上限之内
        player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));

        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        data.setMaxSpirituality(100);
        if (data.getSpirituality() > 100) {
            data.setSpirituality(100);
        }
    }

    /**
     * 每 tick 被动逻辑：小丑暂无被动能力（主动技能「化纸为刀」由技能轮盘触发）
     */
    @Override
    public void tick(Player player) {
        // 暂无被动
    }
}
