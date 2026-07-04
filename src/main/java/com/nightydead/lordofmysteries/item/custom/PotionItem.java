package com.nightydead.lordofmysteries.item.custom;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.entity.IndestructibleItemEntity;
import com.nightydead.lordofmysteries.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import java.util.List;

/**
 * 魔药物品类 - 虚空原样打捞与物理碎裂双全版
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

    // 🚀 彻底删掉 hasCustomEntity 和 createEntity 方法，全部由事件总线在落地时权威托管！

    /**
     * 🔮 因果律碎裂析出
     * 当魔药实体在地面上被火焰、岩浆、爆炸、仙人掌强行打碎时触发
     */
    @Override
    public void onDestroyed(ItemEntity itemEntity, DamageSource damageSource) {
        super.onDestroyed(itemEntity, damageSource);

        Level level = itemEntity.level();
        if (level.isClientSide()) return;

        ItemStack stack = itemEntity.getItem();
        String pathway = stack.get(ModDataComponents.PATHWAY.get());
        Integer seq = stack.get(ModDataComponents.SEQUENCE.get());

        if (pathway != null && seq != null) {
            Item pureCharacteristicItem = ModItems.getPureCharacteristic(pathway, seq);

            if (pureCharacteristicItem != null) {
                ItemStack featureStack = new ItemStack(pureCharacteristicItem);
                featureStack.set(ModDataComponents.PATHWAY.get(), pathway);
                featureStack.set(ModDataComponents.SEQUENCE.get(), seq);
                if (stack.has(ModDataComponents.MAX_SPIRITUALITY.get())) {
                    featureStack.set(ModDataComponents.MAX_SPIRITUALITY.get(), stack.get(ModDataComponents.MAX_SPIRITUALITY.get()));
                }

                // 在魔药碎裂的坐标原地直接生成绝对免伤的纯净非凡特性！
                IndestructibleItemEntity characteristicEntity = new IndestructibleItemEntity(
                        level, itemEntity.getX(), itemEntity.getY() + 0.1, itemEntity.getZ(), featureStack
                );

                characteristicEntity.setDeltaMovement(0, 0.2, 0);
                characteristicEntity.setPickUpDelay(10);

                level.addFreshEntity(characteristicEntity);
                System.out.println("[神秘学律令] 魔药因外力 (" + damageSource.getMsgId() + ") 碎裂，纯净特性已原地重组析出！");
            }
        }
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