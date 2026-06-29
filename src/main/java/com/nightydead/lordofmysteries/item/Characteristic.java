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

/**
 * Characteristic 类，继承自 Item 类，代表游戏中的一个特殊物品
 * 这个物品具有特殊的效果和属性，在游戏中可以食用并施加多种负面效果
 */
public class Characteristic extends Item {

    /**
     * Characteristic 类的构造函数
     * 初始化物品的各种属性，包括堆叠限制、耐火性、稀有度以及食用后的效果
     */
    public Characteristic() {
        super(new Item.Properties()
                .stacksTo(1)           // 设置物品最大堆叠数量为1
                .fireResistant()       // 使物品具有耐火性，不会被火烧毁
                .rarity(Rarity.UNCOMMON) // 设置物品稀有度为 uncommon（罕见）
                .food(new FoodProperties.Builder()
                        .nutrition(1)                    // 设置营养值为1
                        .alwaysEdible()                  // 设置物品总是可以食用
                        .saturationModifier(0.1f)        // 设置饱和度修正为0.1
                        // 添加食用后的效果：虚弱效果，持续400 ticks（20秒），等级为0
                        .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 400, 0), 1.0F)
                        // 添加食用后的效果：减速效果，持续400 ticks（20秒），等级为0
                        .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 0), 1.0F)
                        // 添加食用后的效果：混乱效果，持续400 ticks（20秒），等级为0
                        .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 400, 0), 1.0F)
                        .build()  // 构建食物属性
                )
        );
    }

    /**
    * hasCustomEntity 方法，用于检查物品是否需要自定义实体
    * 在这个方法中，我们返回 true，表示该物品始终需要自定义实体逻辑
    * @param stack 要检查的物品堆栈
    * @return 总是返回true，表示该物品始终需要自定义实体逻辑
    */
    @Override
    public boolean hasCustomEntity(ItemStack stack) {
    // 直接返回true，表示该物品始终需要自定义实体逻辑
        return true;
    }

    /**
     * 用我们不灭的 IndestructibleItemEntity 替换掉原生的 ItemEntity
     */
    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
    // 创建 IndestructibleItemEntity 实例，使用原生物品的位置和物品堆栈
        IndestructibleItemEntity customEntity = new IndestructibleItemEntity(
                level, location.getX(), location.getY(), location.getZ(), stack
        );
        // 复制原生物品的运动状态（抛物线、速度等）
        customEntity.setDeltaMovement(location.getDeltaMovement());
        customEntity.setPickUpDelay(40); // 扔出后 2 秒内不能自己立马吸回来
        return customEntity;
    }

    /**
     * 检查物品是否为特殊版本(foil)的方法
     * 在Minecraft中，foil物品通常指具有特殊光泽或效果的物品，如闪卡
     * 此方法被重写以始终返回true，表示所有该类型的物品都是foil版本
     *
     * @param stack 要检查的物品堆栈
     * @return 总是返回true，表示所有物品都是foil版本
     */
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;  // 直接返回true，表明所有该类型的物品都是foil版本
    }
}
