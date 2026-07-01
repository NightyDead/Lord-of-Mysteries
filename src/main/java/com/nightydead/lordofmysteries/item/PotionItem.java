package com.nightydead.lordofmysteries.item;

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

public class PotionItem extends Item {

    public PotionItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .craftRemainder(Items.GLASS_BOTTLE)
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemstack); // 确保是 consume
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32; // 1.6秒
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // 🌟 【DEBUG 核心】：在第一行立刻打印日志
        if (entity instanceof Player player) {
            String side = level.isClientSide() ? "§b[客户端]" : "§c[服务端]";
            player.sendSystemMessage(Component.literal(side + " §a检测到魔药成功咽下下去了！进度条已走满！"));
        }

        // 调用父类，这会触发 LivingEntityUseItemEvent.Finish 事件去激活你的 ModMysticalMechanics
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);

        if (entity instanceof Player player) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                player.playSound(SoundEvents.GENERIC_DRINK, 0.5F, 1.0F);

                if (stack.isEmpty()) {
                    return new ItemStack(Items.GLASS_BOTTLE);
                }
                ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
                if (!player.getInventory().add(emptyBottle)) {
                    player.drop(emptyBottle, false);
                }
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
        tooltipComponents.add(Component.literal("--- 调配完成的魔药 ---").withStyle(ChatFormatting.DARK_AQUA));
        if (pathway != null && seq != null) {
            tooltipComponents.add(Component.literal("对应途径: " + pathway).withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(Component.literal("目标序列: 序列 " + seq).withStyle(ChatFormatting.DARK_RED));
        } else {
            tooltipComponents.add(Component.literal("⚠️ 警告: 该魔药不含任何非凡神性数据（NBT/Component为空）！").withStyle(ChatFormatting.RED));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}