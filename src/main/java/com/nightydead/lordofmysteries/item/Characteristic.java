package com.nightydead.lordofmysteries.item;

import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.entity.IndestructibleItemEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Characteristic 类，继承自 Item 类，代表游戏中的一个特殊物品
 * 这个物品具有特殊的效果和属性，在游戏中可以食用并施加多种负面效果
 */
public class Characteristic extends Item {

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
                        .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 0), 1.0F)
                        .build()
                )
        );
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        IndestructibleItemEntity customEntity = new IndestructibleItemEntity(
                level, location.getX(), location.getY(), location.getZ(), stack
        );
        customEntity.setDeltaMovement(location.getDeltaMovement());
        customEntity.setPickUpDelay(40); // 扔出后 2 秒内不能自己立马吸回来
        return customEntity;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;  // 直接返回true，表明所有该类型的物品都是foil版本
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        // 1. 尝试获取单体特性组件（如果是单个特性）
        String pathway = stack.get(ModDataComponents.PATHWAY.get());
        Integer seq = stack.get(ModDataComponents.SEQUENCE.get());

        if (pathway != null && seq != null) {
            tooltipComponents.add(Component.literal("途径: " + pathway).withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(Component.literal("序列: " + seq).withStyle(ChatFormatting.RED));
        }

        // 2. ✨ 核心修复：尝试获取聚合特性列表组件（如果是析出的尸体掉落物）
        List<String> history = stack.get(ModDataComponents.AGGREGATED_FEATURES.get());
        if (history != null && !history.isEmpty()) {
            tooltipComponents.add(Component.literal("--- 析出的非凡特性 ---").withStyle(ChatFormatting.DARK_PURPLE));

            for (String record : history) {
                // 格式化解析 "fool:9" 这种字符串
                String[] parts = record.split(":");
                if (parts.length == 2) {
                    String p = parts[0];
                    String s = parts[1];
                    tooltipComponents.add(Component.literal("• " + p + " 序列" + s).withStyle(ChatFormatting.GRAY));
                }
            }
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}