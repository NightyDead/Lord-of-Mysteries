package com.nightydead.lordofmysteries.item.custom;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

/**
 * 魔药主材物品类
 * 专门承载各类魔药的核心合成原材料，具备高贵的不灭特性，提供专属的主材信息看板
 */
public class MainMaterialItem extends Item {

    public MainMaterialItem(Item.Properties customProperties) {
        super(customProperties);
    }

    public MainMaterialItem() {
        super(createDefaultProperties());
    }

    public static Properties createDefaultProperties() {
        return new Item.Properties()
                .stacksTo(1)
                .fireResistant() // 🛡️ 遵从神秘学第一定律：主材同样不可被凡火销毁
                .rarity(Rarity.RARE);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // 让主材在物品栏里同样散发超凡的附魔光芒
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        String pathway = stack.get(ModDataComponents.PATHWAY.get());
        Integer seq = stack.get(ModDataComponents.SEQUENCE.get());

        tooltipComponents.add(Component.translatable("tooltip.lordofmysteries.main_material.title").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD));

        if (pathway != null && seq != null) {
            String pathKey = "pathway." + LordofMysteries.MODID + "." + pathway.toLowerCase();
            // 提示玩家这个材料属于哪个途径、几号序列的魔药主材
            tooltipComponents.add(Component.translatable("tooltip.lordofmysteries.main_material.usage",
                    Component.translatable(pathKey), seq).withStyle(ChatFormatting.GRAY));
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}