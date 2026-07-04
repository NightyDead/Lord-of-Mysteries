package com.nightydead.lordofmysteries.item.custom;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import java.util.List;

/**
 * 魔药物品类
 * 负责处理魔药的饮用动画、完饮判定、空瓶返还及专属魔药 Tooltip 看板
 */
public class PotionItem extends Item {

    public PotionItem(Properties customProperties) {
        super(customProperties);
    }

    public PotionItem() {
        this(createDefaultProperties());
    }

    public static Properties createDefaultProperties() {
        return new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .craftRemainder(Items.GLASS_BOTTLE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) { return 32; }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);
        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            stack.shrink(1);
            player.playSound(SoundEvents.GENERIC_DRINK, 0.5F, 1.0F);

            ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
            if (stack.isEmpty()) return emptyBottle;

            if (!player.getInventory().add(emptyBottle)) {
                player.drop(emptyBottle, false);
            }
        }
        return resultStack;
    }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        String pathway = stack.get(ModDataComponents.PATHWAY.get());
        Integer seq = stack.get(ModDataComponents.SEQUENCE.get());

        tooltipComponents.add(Component.translatable("tooltip.lordofmysteries.potion.title").withStyle(ChatFormatting.DARK_AQUA));
        if (pathway != null && seq != null) {
            String pathKey = "pathway." + LordofMysteries.MODID + "." + pathway.toLowerCase();
            tooltipComponents.add(Component.translatable("tooltip.lordofmysteries.potion.pathway", Component.translatable(pathKey)).withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(Component.translatable("tooltip.lordofmysteries.potion.sequence", seq).withStyle(ChatFormatting.DARK_RED));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.lordofmysteries.potion.empty_warning").withStyle(ChatFormatting.RED));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}