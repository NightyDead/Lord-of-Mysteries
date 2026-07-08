package com.nightydead.lordofmysteries.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * 纯水产品类 - 继承原版药水行为
 * 拥有与原版水瓶完全一致的饮用、效果、Tooltip 机制
 * 作为炼药锅的基础溶剂使用
 */
public class PureWaterItem extends PotionItem {

    public PureWaterItem(Properties properties) {
        super(properties);
    }

    /**
     * 创建纯水的默认属性
     * 不可堆叠
     *
     * @return 配置好的物品属性
     */
    public static Properties createDefaultProperties() {
        return new Properties()
                .stacksTo(1);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.lordofmysteries.pure_water.desc")
                .withStyle(ChatFormatting.AQUA));
        // 调用父类显示药水效果提示（纯水无效果，但保留框架以便后续扩展）
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
