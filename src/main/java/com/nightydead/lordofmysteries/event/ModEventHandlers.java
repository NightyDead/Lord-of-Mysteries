package com.nightydead.lordofmysteries.event;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.data.PlayerData;
import com.nightydead.lordofmysteries.entity.IndestructibleItemEntity;
import com.nightydead.lordofmysteries.item.ModItems;
import com.nightydead.lordofmysteries.item.custom.CharacteristicItem;
import com.nightydead.lordofmysteries.pathway.PathwayRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;

@EventBusSubscriber(modid = LordofMysteries.MODID)
public class ModEventHandlers {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer serverPlayer) {
            ModMysticalMechanics.syncAllData(serverPlayer, serverPlayer.getData(ModAttachments.PLAYER_DATA.get()));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || player.isSpectator() || !player.isAlive()) return;

        var data = player.getData(ModAttachments.PLAYER_DATA.get());
        String currentPathwayId = data.getCurrentPathway();
        int currentSeqNum = data.getCurrentSequence();

        // 1. 执行序列能力 Tick调度
        if (currentPathwayId != null && !currentPathwayId.equalsIgnoreCase("none") && currentSeqNum < 10) {
            var pathwayObj = PathwayRegistry.get(currentPathwayId);
            if (pathwayObj != null) {
                var sequenceObj = pathwayObj.getSequence(currentSeqNum);
                if (sequenceObj != null) sequenceObj.tick(player);
            }
        }

        // 2. 失控倒计时调度（生存模式专用）
        if (!player.isCreative()) {
            int ticksLeft = data.getSdcTicks();
            if (ticksLeft > 0) {
                data.setSdcTicks(ticksLeft - 1);
                if (ticksLeft % 60 == 0) {
                    player.sendSystemMessage(Component.translatable("message.lordofmysteries.madness.whisper"));
                }
                if (data.getSdcTicks() <= 0) {
                    data.setSdcTicks(-2); // 触发生成倒计时锁
                    player.sendSystemMessage(Component.translatable("message.lordofmysteries.madness.failed"));
                    player.hurt(player.damageSources().outOfBorder(), Float.MAX_VALUE);
                }
            }
        }

        // 3. 自然恢复机制（200t恢复理智，40t恢复灵性）
        if (player.tickCount % 200 == 0 && data.getSanity() < 100 && (data.getSdcTicks() == -1 || player.isCreative())) {
            data.setSanity(data.getSanity() + 1);
        }

        if (player.tickCount % 40 == 0) {
            int maxSp = data.getMaxSpiritual();
            if (maxSp > 0 && data.getSpirituality() < maxSp) {
                data.addSpirituality(Math.max(1, (int) (maxSp * 0.05f)));
            }
        }

        // 4. 定期兜底数据传输（合并优化减少发包）
        if (player.tickCount % 20 == 0 && player instanceof ServerPlayer serverPlayer) {
            ModMysticalMechanics.syncAllData(serverPlayer, data);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            player.getExistingData(ModAttachments.PLAYER_DATA.get()).ifPresent(data -> {
                if (data.getSdcTicks() == -2) {
                    spawnMadnessMonster(player);
                    data.setSdcTicks(-1);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide() || player.isAlive()) return;

        var data = player.getData(ModAttachments.PLAYER_DATA.get());
        var history = data.getAbsorbedCharacteristics();

        if (history != null && !history.isEmpty()) {
            ItemStack aggregatedStack = new ItemStack(ModItems.AGGREGATED_CHARACTERISTIC.get());
            aggregatedStack.set(ModDataComponents.AGGREGATED_FEATURES.get(), new ArrayList<>(history));

            event.getDrops().add(new ItemEntity(player.level(), player.getX(), player.getY() + 0.5, player.getZ(), aggregatedStack));
            data.reset(); // 特性析出后彻底归凡
            player.sendSystemMessage(Component.translatable("message.lordofmysteries.characteristic.dropped"));
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        var oldData = event.getOriginal().getData(ModAttachments.PLAYER_DATA.get());
        var newData = event.getEntity().getData(ModAttachments.PLAYER_DATA.get());

        newData.setCurrentPathway(oldData.getCurrentPathway());
        newData.setCurrentSequence(oldData.getCurrentSequence());
        newData.setMaxSpirituality(oldData.getMaxSpiritual());
        newData.setSpirituality(oldData.getSpirituality());
        newData.setDigestion(oldData.getDigestion());
        newData.setAbsorbedCharacteristics(new ArrayList<>(oldData.getAbsorbedCharacteristics()));

        if (event.isWasDeath()) {
            newData.setSanity(100);
            newData.setSdcTicks(-1);
        } else {
            newData.setSanity(oldData.getSanity());
            newData.setSdcTicks(oldData.getSdcTicks());
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ModMysticalMechanics.syncAllData(serverPlayer, newData);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();

            if (stack.getItem() instanceof CharacteristicItem) {
                // 🛡️ 稳健的环境双重锁逻辑拦截指令流
                if (itemEntity.getOwner() == null && itemEntity.tickCount == 0) return;
                if (itemEntity instanceof IndestructibleItemEntity) return;

                event.setCanceled(true);
                IndestructibleItemEntity customEntity = new IndestructibleItemEntity(
                        event.getLevel(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), stack
                );
                customEntity.setDeltaMovement(itemEntity.getDeltaMovement());
                customEntity.setPickUpDelay(10);
                event.getLevel().addFreshEntity(customEntity);
            }
        }
    }

    private static void spawnMadnessMonster(Player player) {
        Level level = player.level();
        Zombie zombie = EntityType.ZOMBIE.create(level);
        if (zombie != null) {
            zombie.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
            zombie.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 99999, 1));
            zombie.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 99999, 0));
            zombie.setCustomName(Component.translatable("entity.lordofmysteries.madness_zombie", player.getGameProfile().getName()));
            zombie.setCustomNameVisible(true);
            level.addFreshEntity(zombie);
        }
    }
}