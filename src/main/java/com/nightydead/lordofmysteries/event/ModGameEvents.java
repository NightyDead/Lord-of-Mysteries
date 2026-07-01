package com.nightydead.lordofmysteries.event;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.data.PlayerData;
import com.nightydead.lordofmysteries.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Lord of Mystries 模组的事件处理类
 */
@EventBusSubscriber(modid = LordofMysteries.MODID)
public class ModGameEvents {

    /**
     * ⏳ 核心循环：处理高维度疯狂倒计时（15秒）
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // 🛡️ 只在服务端且确保玩家处于存活状态
        if (!player.level().isClientSide && player.isAlive()) {
            player.getExistingData(ModAttachments.PLAYER_DATA.get()).ifPresent(data -> {
                int ticksLeft = data.getSdcTicks();

                if (ticksLeft > 0) {
                    data.setSdcTicks(ticksLeft - 1);

                    // 每 3 秒（100 ticks）发送一次血红色疯狂低语
                    if (ticksLeft % 60 == 0) {
                        player.sendSystemMessage(Component.literal("§5你听到了无法理解的呢喃... 意识正在消散..."));
                    }

                    // 🎯 当倒计时 <= 0 时，触发毁灭
                    if (data.getSdcTicks() <= 0) {
                        data.setSdcTicks(-1); // 立即锁定状态位，防止死循环
                        player.sendSystemMessage(Component.literal("§4你的理智彻底崩溃，你成为了失控的牺牲品。"));

                        Level level = player.level();
                        Zombie zombie = EntityType.ZOMBIE.create(level);
                        if (zombie != null) {
                            zombie.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
                            zombie.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 99999, 1));
                            zombie.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 99999, 0));
                            zombie.setCustomName(Component.literal("§4失控的" + player.getGameProfile().getName()));
                            zombie.setCustomNameVisible(true);

                            level.addFreshEntity(zombie);
                        }

                        // 🔥 【修改点1】：不要使用 player.kill()。
                        // 使用外界绝对伤害（OUT_OF_WORLD / OUT_OF_BORDER）直接扣除核心生命值，确保触发完整的死亡流程。
                        player.hurt(player.damageSources().outOfBorder(), Float.MAX_VALUE);
                    }
                }
            });
        }
    }

    /**
     * ⚰️ 【修改点2】：改用 LivingDropsEvent 监听掉落
     * 此时是 Minecraft 正在收集实体死亡掉落物的黄金时机
     */
    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {

            // 获取非凡特性数据
            PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
            List<String> history = data.getAbsorbedCharacteristics();

            System.out.println("player " + player.getName().getString() + " now: " + history);

            if (history != null && !history.isEmpty()) {
                // 1. 实例化聚合特性物品
                ItemStack aggregatedStack = new ItemStack(ModItems.AGGREGATED_CHARACTERISTIC.get());
                aggregatedStack.set(ModDataComponents.AGGREGATED_FEATURES.get(), new ArrayList<>(history));

                // 2. 创建 ItemEntity
                ItemEntity dropEntity = new ItemEntity(
                        player.level(),
                        player.getX(),
                        player.getY() + 0.5,
                        player.getZ(),
                        aggregatedStack
                );

                // 【核心变动】：不要直接 addFreshEntity，而是加入到事件的掉落物列表中！
                // 这样能保证契合死亡掉落规则（比如保持物品不灭、迎合死亡不掉落指令等，虽然特性的不灭通常无视死亡不掉落）
                event.getDrops().add(dropEntity);

                // 3. 重置玩家数据，防止复活后依然带有特性
                data.reset();

                player.sendSystemMessage(Component.literal("§d[特性不灭] §5你体内的非凡特性已析出。"));
            } else {
                System.out.println("no drop：history null! Ticks : " + data.getSdcTicks());
            }
        }
    }
}