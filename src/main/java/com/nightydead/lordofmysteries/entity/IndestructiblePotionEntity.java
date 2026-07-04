package com.nightydead.lordofmysteries.entity;

import com.nightydead.lordofmysteries.item.custom.PotionItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * 不可破坏的魔药物品实体类
 * 具备稳健的抗秒吸拾取延迟，完美继承虚空打捞机制，同时允许俗世物理力量碎裂析出特性
 */
public class IndestructiblePotionEntity extends IndestructibleItemEntity {

    public IndestructiblePotionEntity(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        this.setInvulnerable(false); // 🔓 解除绝对无敌，允许环境伤害输入
        this.setPickUpDelay(40);     // ⏳ 锁死标准丢出延迟（2秒内绝对无法被拾取）
    }

    public IndestructiblePotionEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(level, x, y, z, stack);
        this.setInvulnerable(false); // 🔓 解除绝对无敌，允许环境伤害输入
        this.setPickUpDelay(40);     // ⏳ 锁死标准丢出延迟（2秒内绝对无法被拾取）
    }

    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float damage) {
        if (damageSource.isCreativePlayer()) {
            this.discard();
            return true;
        }

        if (damageSource.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return false;
        }

        if (!damageSource.is(DamageTypes.IN_WALL)) {
            this.markHurt();
            if (this.getItem().getItem() instanceof PotionItem potion) {
                potion.onDestroyed(this, damageSource);
            }
            this.discard();
            return true;
        }

        return false;
    }
}