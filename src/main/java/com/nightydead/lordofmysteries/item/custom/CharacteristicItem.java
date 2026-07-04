package com.nightydead.lordofmysteries.item.custom;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

/**
 * 非凡特性物品类
 * 承载不灭定律的非凡基因载体，允许生吞，提供完整的非凡聚合Tooltip渲染
 */
public class CharacteristicItem extends Item {

    /**
     * 提供一个接收自定义 Properties 的构造函数，供 ModItems 注册时动态注入组件
     */
    public CharacteristicItem(Item.Properties customProperties) {
        super(customProperties);
    }

    /**
     * 显式保留无参构造函数，并赋予标准的非凡特性基础属性
     */
    public CharacteristicItem() {
        super(createDefaultProperties());
    }

    public static Properties createDefaultProperties() {
        return new Item.Properties()
                .stacksTo(1)
                .fireResistant()
                .rarity(Rarity.UNCOMMON)
                .food(new FoodProperties.Builder()
                        .nutrition(1)
                        .alwaysEdible()
                        .saturationModifier(0.1f)
                        .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 400, 0), 1.0F)
                        .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 0), 1.0F)
                        .build()
                );
    }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        String pathway = stack.get(ModDataComponents.PATHWAY.get());
        Integer seq = stack.get(ModDataComponents.SEQUENCE.get());

        if (pathway != null && seq != null) {
            String pathKey = "pathway." + LordofMysteries.MODID + "." + pathway.toLowerCase();
            tooltipComponents.add(Component.translatable("tooltip.lordofmysteries.characteristic.info", Component.translatable(pathKey), seq).withStyle(ChatFormatting.GOLD));
        }

        List<String> history = stack.get(ModDataComponents.AGGREGATED_FEATURES.get());
        if (history != null && !history.isEmpty()) {
            tooltipComponents.add(Component.translatable("tooltip.lordofmysteries.characteristic.aggregated_title").withStyle(ChatFormatting.DARK_PURPLE));
            for (String record : history) {
                String[] parts = record.split(":");
                if (parts.length == 2) {
                    String pathKey = "pathway." + LordofMysteries.MODID + "." + parts[0].toLowerCase();
                    tooltipComponents.add(Component.literal("• ").append(Component.translatable("tooltip.lordofmysteries.characteristic.info", Component.translatable(pathKey), Integer.parseInt(parts[1]))).withStyle(ChatFormatting.GRAY));
                }
            }
        }

        tooltipComponents.add(Component.translatable("tooltip.lordofmysteries.characteristic.law").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}