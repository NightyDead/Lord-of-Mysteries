package com.nightydead.lordofmysteries.event;

import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.network.SyncDigestionPacket;
import com.nightydead.lordofmysteries.network.SyncPathwayPacket;
import com.nightydead.lordofmysteries.network.SyncSanityPacket;
import com.nightydead.lordofmysteries.network.SyncSpiritualityPacket;


import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "lordofmysteries")
public class ModEventHandlers {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide) {
            var data = event.getEntity().getData(ModAttachments.PLAYER_DATA.get());
            // 玩家进服时，强行进行一次三包全量校准
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new SyncSanityPacket(data.getSanity()));
                PacketDistributor.sendToPlayer(serverPlayer, new SyncSpiritualityPacket(data.getSpirituality(), data.getMaxSpiritual()));
                PacketDistributor.sendToPlayer(serverPlayer, new SyncDigestionPacket(data.getDigestion()));
                PacketDistributor.sendToPlayer(serverPlayer, new SyncPathwayPacket(data.getCurrentPathway(), data.getCurrentSequence()));
            }
        }
    }

    /**
     * ⚡ 核心玩家游戏刻事件（已完美修复创造模式不回复的 Bug）
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        // 🛡️ 仅过滤客户端和旁观模式（旁观模式不属于物质世界，不回复）
        if (player.level().isClientSide || player.isSpectator()) return;

        var data = player.getData(ModAttachments.PLAYER_DATA.get());

        // 🧠 【部分 A】：处理高维度疯狂失控倒计时（🌟 创造模式免疫失控）
        if (!player.isCreative()) { // 💡 将创造模式保护精准套在失控惩罚上！
            int ticks = data.getSdcTicks();
            if (ticks > 0) {
                data.setSdcTicks(--ticks);
                if (ticks == 0) {
                    data.setSdcTicks(-2);
                    player.die(player.damageSources().magic());
                }
            }
        }

        // 🧠 【部分 B】：理智随时间自然恢复 (每 10 秒恢复 1 点) —— 🌟 创造模式现在也可以恢复了！
        if (player.tickCount % 200 == 0) {
            int currentSanity = data.getSanity();
            if (currentSanity < 100 && (data.getSdcTicks() == -1 || player.isCreative())) {
                data.setSanity(currentSanity + 1);
                if (player instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(serverPlayer, new SyncSanityPacket(data.getSanity()));
                }
            }
        }

        // 🔮 【部分 C】：灵性随时间自然恢复 (每 2 秒恢复 5%) —— 🌟 创造模式可以疯狂回蓝测试技能了！
        if (player.tickCount % 40 == 0) {
            int maxSp = data.getMaxSpiritual();
            int currentSp = data.getSpirituality();

            if (maxSp > 0 && currentSp < maxSp) {
                int recoveryAmount = Math.max(1, (int) (maxSp * 0.05f));
                data.setSpirituality(Math.min(maxSp, currentSp + recoveryAmount));
                if (player instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(serverPlayer, new SyncSpiritualityPacket(data.getSpirituality(), maxSp));
                }
            }
        }

        // 📡 【部分 D】：周期性主管道校准 (每 1 秒兜底发包)
        if (player.tickCount % 20 == 0 && player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncSpiritualityPacket(data.getSpirituality(), data.getMaxSpiritual()));
            PacketDistributor.sendToPlayer(serverPlayer, new SyncSanityPacket(data.getSanity()));
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
    public static void onPlayerClone(PlayerEvent.Clone event) {
        var oldPlayer = event.getOriginal();
        var newPlayer = event.getEntity();

        if (!newPlayer.level().isClientSide()) {
            var oldData = oldPlayer.getData(ModAttachments.PLAYER_DATA.get());
            var newData = newPlayer.getData(ModAttachments.PLAYER_DATA.get());

            newData.setCurrentPathway(oldData.getCurrentPathway());
            newData.setCurrentSequence(oldData.getCurrentSequence());
            newData.setMaxSpirituality(oldData.getMaxSpiritual());
            newData.setSpirituality(oldData.getSpirituality());
            newData.setDigestion(oldData.getDigestion());

            if (event.isWasDeath()) {
                newData.setSanity(100);
                newData.setSdcTicks(-1);
            } else {
                newData.setSanity(oldData.getSanity());
                newData.setSdcTicks(oldData.getSdcTicks());
            }

            if (newPlayer instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new SyncSanityPacket(newData.getSanity()));
                PacketDistributor.sendToPlayer(serverPlayer, new SyncSpiritualityPacket(newData.getSpirituality(), newData.getMaxSpiritual()));
                PacketDistributor.sendToPlayer(serverPlayer, new SyncDigestionPacket(newData.getDigestion()));
                PacketDistributor.sendToPlayer(serverPlayer, new SyncPathwayPacket(newData.getCurrentPathway(), newData.getCurrentSequence()));
            }
        }
    }

    private static void spawnMadnessMonster(Player player) {
        Level level = player.level();
        Zombie zombie = EntityType.ZOMBIE.create(level);
        if (zombie != null) {
            zombie.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
            zombie.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 99999, 1));
            zombie.setCustomName(Component.literal("§4失控的 " + player.getGameProfile().getName()));
            zombie.setCustomNameVisible(true);
            level.addFreshEntity(zombie);
        }
    }
}