package com.nightydead.lordofmysteries.event;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.block.AlchemyCauldronBlock;
import com.nightydead.lordofmysteries.block.AlchemyCauldronBlockEntity;
import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.data.PlayerData;
import com.nightydead.lordofmysteries.entity.IndestructibleItemEntity;
import com.nightydead.lordofmysteries.entity.IndestructiblePotionEntity;
import com.nightydead.lordofmysteries.item.ModItems;
import com.nightydead.lordofmysteries.item.custom.CharacteristicItem;
import com.nightydead.lordofmysteries.item.custom.MainMaterialItem;
import com.nightydead.lordofmysteries.item.custom.ModPotionItem;
import com.nightydead.lordofmysteries.item.custom.RitualDaggerItem;
import com.nightydead.lordofmysteries.pathway.PathwayRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import com.nightydead.lordofmysteries.network.SyncVisionPacket;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 模组事件处理器类
 * 统一处理玩家登录、Tick调度、死亡、掉落物、实体生成等核心游戏事件
 * 通过 @EventBusSubscriber 注解自动注册到模组事件总线
 */
@EventBusSubscriber(modid = LordofMysteries.MODID)
public class ModEventHandlers {

    /** 灵视感知半径（方块单位），与客户端 VisionGlowHandler 保持一致 */
    private static final double VISION_RANGE = 32.0D;
    /** 服务端发光扫描频率：每 10 tick 扫描一次 */
    private static final int VISION_SCAN_INTERVAL = 10;
    /** 灵视灵性消耗频率：每 8 tick（0.4秒）消耗 2 点灵性（约 5点/秒，100点 ~20秒耗尽） */
    private static final int VISION_SPIRITUALITY_DRAIN_INTERVAL = 8;
    /** 灵视每次消耗的灵性点数 */
    private static final int VISION_SPIRITUALITY_DRAIN_AMOUNT = 2;

    /**
     * 服务端灵视发光追踪：记录每个玩家通过灵视标记为发光的实体 ID 集合
     * 用于在灵视关闭时精确还原发光状态，避免误清其他来源的发光效果
     */
    private static final Map<UUID, Set<Integer>> serverVisionGlowingEntities = new HashMap<>();

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
                    player.displayClientMessage(Component.translatable("message.lordofmysteries.madness.whisper"), true);
                }
                if (data.getSdcTicks() <= 0) {
                    data.setSdcTicks(-2); // 触发生成倒计时锁
                    player.displayClientMessage(Component.translatable("message.lordofmysteries.madness.failed"), true);
                    player.hurt(player.damageSources().outOfBorder(), Float.MAX_VALUE);
                }
            }
        }

        // 3. 自然恢复机制（200t恢复理智，40t恢复灵性）
        if (player.tickCount % 200 == 0 && data.getSanity() < 100 && (data.getSdcTicks() == -1 || player.isCreative())) {
            data.setSanity(data.getSanity() + 1);
        }

        // 灵视激活时暂停自然灵性恢复，避免与消耗抵消导致无代价使用
        if (player.tickCount % 40 == 0 && !data.isVisionActive()) {
            int maxSp = data.getMaxSpiritual();
            if (maxSp > 0 && data.getSpirituality() < maxSp) {
                data.addSpirituality(Math.max(1, (int) (maxSp * 0.05f)));
            }
        }

        // 4. 定期兜底数据传输（合并优化减少发包）
        if (player.tickCount % 20 == 0 && player instanceof ServerPlayer serverPlayer) {
            ModMysticalMechanics.syncAllData(serverPlayer, data);
        }

        // 5. 🔮 灵视发光处理（服务端权威模式）
        // 在服务端设置 setGlowingTag，通过实体数据同步到客户端渲染，避免单人模式下客户端设置被覆盖
        handleVisionGlowing(player, data);
    }

    /**
     * 服务端灵视发光处理
     * 当玩家灵视激活时，扫描周围活体生物并在服务端设置发光标记
     * 关闭灵视时，精确清除所有由灵视标记的发光实体
     *
     * @param player 当前 Tick 的玩家
     * @param data   玩家非凡数据
     */
    private static void handleVisionGlowing(Player player, PlayerData data) {
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();
        boolean visionActive = data.isVisionActive();

        if (!visionActive) {
            // 灵视关闭时：清除该玩家标记的所有发光实体
            Set<Integer> tracked = serverVisionGlowingEntities.remove(playerId);
            if (tracked != null && !tracked.isEmpty()) {
                for (int entityId : tracked) {
                    var entity = player.level().getEntity(entityId);
                    if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
                        livingEntity.setGlowingTag(false);
                    }
                }
                tracked.clear();
            }
            return;
        }

        // ==================== 🔮 灵视灵性消耗机制 ====================
        // 灵视持续消耗灵性：每 8 tick（0.4秒）消耗 2 点灵性，约 20 秒耗尽
        if (player.tickCount % VISION_SPIRITUALITY_DRAIN_INTERVAL == 0 && !player.isCreative()) {
            int currentSp = data.getSpirituality();
            if (currentSp > 0) {
                data.addSpirituality(-VISION_SPIRITUALITY_DRAIN_AMOUNT);
            }

            // 灵性耗尽：自动关闭灵视 + 施加头晕眼花 debuff
            if (data.getSpirituality() <= 0) {
                data.setVisionActive(false);
                player.setData(ModAttachments.PLAYER_DATA.get(), data);

                // 同步灵视关闭状态到客户端
                if (player instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(serverPlayer, new SyncVisionPacket(false));
                }

                // 施加反胃 + 失明效果（头晕眼花），持续 10 秒（200 tick）
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));

                player.displayClientMessage(Component.literal("§c灵性枯竭！灵视被迫关闭，你感到一阵头晕目眩..."), true);
                return; // 灵视已关闭，跳过后续发光逻辑
            }
        }

        // 灵性为 0 但灵视仍激活（极端情况兜底）：强制关闭 + debuff
        if (data.getSpirituality() <= 0 && !player.isCreative()) {
            data.setVisionActive(false);
            player.setData(ModAttachments.PLAYER_DATA.get(), data);
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new SyncVisionPacket(false));
            }
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
            player.displayClientMessage(Component.literal("§c灵性枯竭！灵视被迫关闭，你感到一阵头晕目眩..."), true);
            return;
        }

        // ==================== 🔮 灵视发光扫描 ====================
        // 灵视激活时：每 VISION_SCAN_INTERVAL tick 扫描一次
        if (player.tickCount % VISION_SCAN_INTERVAL != 0) return;

        Set<Integer> tracked = serverVisionGlowingEntities.computeIfAbsent(playerId, k -> new HashSet<>());

        // 清理已失效的实体记录
        Iterator<Integer> iterator = tracked.iterator();
        while (iterator.hasNext()) {
            int entityId = iterator.next();
            var entity = player.level().getEntity(entityId);
            if (entity == null || !entity.isAlive()) {
                iterator.remove();
            }
        }

        // 扫描并标记周围活体生物为发光状态
        var nearbyEntities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(VISION_RANGE),
                entity -> entity != player && entity.isAlive()
        );

        for (LivingEntity entity : nearbyEntities) {
            if (!entity.isCurrentlyGlowing()) {
                entity.setGlowingTag(true); // 服务端设置，通过实体数据同步到客户端
                tracked.add(entity.getId());
            }
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
            // 特性析出前触发序列移除回调（回退灵性上限、剥离被动能力等）
            ModMysticalMechanics.invokeOnRemoved(player, data.getCurrentPathway(), data.getCurrentSequence());
            data.reset(); // 特性析出后彻底归凡
            player.displayClientMessage(Component.translatable("message.lordofmysteries.characteristic.dropped"), true);
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
     * 玩家右键方块事件 - 处理仪式匕首与炼药锅的交互
     * 手持仪式匕首 shift+右键炼药锅时，普通人也能注入灵性触发酿造
     * 永远阻止匕首被当作材料放入炼药锅
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        // 只在服务端处理实际逻辑
        if (event.getLevel().isClientSide()) return;

        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof RitualDaggerItem)) return;

        // 只拦截炼药锅交互
        BlockPos pos = event.getPos();
        BlockState state = event.getLevel().getBlockState(pos);
        if (!(state.getBlock() instanceof AlchemyCauldronBlock)) return;

        BlockEntity be = event.getLevel().getBlockEntity(pos);
        if (!(be instanceof AlchemyCauldronBlockEntity cauldron)) return;

        Player player = event.getEntity();

        // shift+右键 + 锅内有物品且未酿造 → 触发酿造
        if (player.isShiftKeyDown() && !cauldron.isEmpty() && !cauldron.isBrewed()) {
            cauldron.triggerBrew(player);
            int newState = cauldron.getBrewState();
            event.getLevel().setBlock(pos, state.setValue(AlchemyCauldronBlock.BREW_STATE, newState),
                    Block.UPDATE_ALL);
            player.displayClientMessage(Component.translatable(
                    "message.lordofmysteries.ritual_dagger.cauldron_brew"), true);
        }

        // 始终取消事件，防止匕首被当作材料误吞
        event.setCanceled(true);
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