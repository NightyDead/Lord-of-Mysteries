package com.nightydead.lordofmysteries.event;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.entity.IndestructibleItemEntity;
import com.nightydead.lordofmysteries.entity.IndestructiblePotionEntity;
import com.nightydead.lordofmysteries.item.ModItems;
import com.nightydead.lordofmysteries.item.custom.CharacteristicItem;
import com.nightydead.lordofmysteries.item.custom.MainMaterialItem;
import com.nightydead.lordofmysteries.item.custom.ModPotionItem;
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

/**
 * 模组事件处理器类
 * 统一处理玩家登录、Tick调度、死亡、掉落物、实体生成等核心游戏事件
 * 通过 @EventBusSubscriber 注解自动注册到模组事件总线
 */
@EventBusSubscriber(modid = LordofMysteries.MODID)
public class ModEventHandlers {

    /**
     * 玩家登录事件 - 在玩家加入世界时同步所有神秘学数据到客户端
     *
     * @param event 玩家登录事件
     */
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer serverPlayer) {
            ModMysticalMechanics.syncAllData(serverPlayer, serverPlayer.getData(ModAttachments.PLAYER_DATA.get()));
        }
    }

    /**
     * 玩家 Tick 事件 - 每游戏刻执行神秘学系统调度
     * 包括：序列能力调度、失控倒计时、自然恢复机制、定期数据同步
     *
     * @param event 玩家 Tick 事件
     */
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

    /**
     * 玩家死亡事件 - 处理失控倒计时触发的疯狂怪物生成
     *
     * @param event 生物死亡事件
     */
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

    /**
     * 玩家掉落物事件 - 玩家死亡时析出已吸收的非凡特性为聚合特性物品
     *
     * @param event 生物掉落物事件
     */
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

    /**
     * 玩家克隆事件 - 在玩家重生或维度转移时复制神秘学数据到新实例
     *
     * @param event 玩家克隆事件
     */
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

    /**
     * 实体加入世界事件 - 拦截原版物品实体，将其替换为自定义不灭实体
     * 魔药、非凡特性、魔药主材均在此处转化为不可破坏的实体形式
     *
     * @param event 实体加入世界事件
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // 🛡️ 权威防护 1：只在服务端执行数据操作，杜绝客户端幽灵闪烁分身
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();

            // 🛡️ 权威防护 2：严禁套娃！已经是我们定制的不灭实体对象直接放行
            if (itemEntity instanceof IndestructibleItemEntity) return;
            if (itemEntity.getOwner() == null && itemEntity.tickCount == 0) return;

            // 🔮 1. 拦截魔药落地：将其安全转化为专属于魔药的【不灭实体】！
            if (stack.getItem() instanceof ModPotionItem) {
                event.setCanceled(true); // 终止原版实体的加载

                IndestructiblePotionEntity customPotionEntity =
                        new IndestructiblePotionEntity(
                                event.getLevel(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), stack
                        );
                customPotionEntity.setDeltaMovement(itemEntity.getDeltaMovement());
                customPotionEntity.setPickUpDelay(40);

                event.getLevel().addFreshEntity(customPotionEntity); // 注入世界，完美享有魔药不灭与打捞因果
                return;
            }

            // 🔮 2. 拦截特性和主材：依旧在此处安全转化为绝对免伤的 IndestructibleItemEntity
            if (stack.getItem() instanceof CharacteristicItem || stack.getItem() instanceof MainMaterialItem) {
                event.setCanceled(true);

                IndestructibleItemEntity customEntity = new IndestructibleItemEntity(
                        event.getLevel(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), stack
                );
                customEntity.setDeltaMovement(itemEntity.getDeltaMovement());
                customEntity.setPickUpDelay(40);

                event.getLevel().addFreshEntity(customEntity);
            }
        }
    }

    /**
     * 生成疯狂怪物 - 玩家失控倒计时归零后在玩家位置生成强化僵尸
     * 怪物拥有力量提升和速度提升的永久效果，并显示玩家名称
     *
     * @param player 失控的玩家
     */
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