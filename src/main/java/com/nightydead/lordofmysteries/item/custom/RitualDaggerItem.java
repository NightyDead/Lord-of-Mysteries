package com.nightydead.lordofmysteries.item.custom;

import com.nightydead.lordofmysteries.LordofMysteries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

/**
 * 仪式匕首
 * 攻击与铁剑相同，攻速更快、攻击范围更短
 * 手持此匕首 shift + 右键炼药锅时，即使是普通人也能注入灵性触发酿造
 */
public class RitualDaggerItem extends SwordItem {

    public RitualDaggerItem() {
        super(Tiers.IRON, new Properties().attributes(createAttributes()));
    }

    /**
     * 禁用格挡：仪式匕首不是盾牌，右键不触发格挡动画
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    /**
     * 构建仪式匕首的属性修饰器
     * 攻击力 = 6.0（与铁剑相同），攻速 = 2.4（铁剑为 1.6），攻击范围缩短 1.5
     */
    private static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID,
                                3.0 + Tiers.IRON.getAttackDamageBonus(),
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID,
                                -1.6,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "ritual_dagger_range"),
                                -1.5,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }
}
