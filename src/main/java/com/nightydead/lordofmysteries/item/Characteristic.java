package com.nightydead.lordofmysteries.item;

import com.nightydead.lordofmysteries.entity.IndestructibleItemEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class Characteristic extends Item {

    public Characteristic() {
        super(new Item.Properties()
                .stacksTo(1)
                .fireResistant()
                .rarity(Rarity.UNCOMMON)
                .food(new FoodProperties.Builder()
                        .nutrition(1)
                        .alwaysEdible()
                        .saturationModifier(0.1f)
                        .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 400, 0), 1.0F)
                        .build()
                )
        );
    }

    /*
     * 告知系统：当该物品掉落在世界上时，启用自定义实体逻辑
     */
    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    /*
     * 用我们不灭的 IndestructibleItemEntity 替换掉原生的 ItemEntity
     */
    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        IndestructibleItemEntity customEntity = new IndestructibleItemEntity(
                level, location.getX(), location.getY(), location.getZ(), stack
        );
        // 复制原生物品的运动状态（抛物线、速度等）
        customEntity.setDeltaMovement(location.getDeltaMovement());
        customEntity.setPickUpDelay(40); // 扔出后 2 秒内不能自己立马吸回来
        return customEntity;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
