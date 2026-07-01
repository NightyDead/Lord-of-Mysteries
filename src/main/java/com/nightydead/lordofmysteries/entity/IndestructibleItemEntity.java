package com.nightydead.lordofmysteries.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 不可破坏的物品实体类，继承自 ItemEntity
 * 完美契合神秘学非凡特性不灭定律：绝对防火、防爆、防虚空湮灭、支持虚空打捞悬浮。
 */
public class IndestructibleItemEntity extends ItemEntity {

    // 🛡️ 引入转场保护锁，防止 1.21.1 跨维度延迟执行期间 checkBelowWorld 被无限重复触发
    private boolean isTransferringDimension = false;

    // 🧠 核心修复：引入一个运行时的内存状态锁，防止在 tick 中由于频繁 stack.update 导致编译/运行死循环
    private boolean isHoveringRuntime = false;
    private boolean isHoveringInitialized = false;

    public IndestructibleItemEntity(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        this.setInvulnerable(true);
        this.setUnlimitedLifetime();
    }

    public IndestructibleItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(level, x, y, z, stack);
        this.setInvulnerable(true);
        this.setUnlimitedLifetime();
    }

    /**
     * 检查物品是否处于绝对悬浮状态
     * @return 如果物品具有绝对悬浮标记则返回true，否则返回false
     */
    public boolean isAbsoluteHovering() {
        // 优先使用高效率的内存运行时缓存状态，阻断由于强读 NBT 导致的延迟
        if (this.isHoveringInitialized) {
            return this.isHoveringRuntime;
        }

        ItemStack stack = this.getItem();
        if (stack.isEmpty()) return false;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            this.isHoveringRuntime = customData.copyTag().getBoolean("IsAbsoluteHovering");
            this.isHoveringInitialized = true;
            return this.isHoveringRuntime;
        }
        return false;
    }

    public void setAbsoluteHovering(boolean hovering) {
        this.isHoveringRuntime = hovering;
        this.isHoveringInitialized = true;
        this.setNoGravity(hovering);

        // 只在有必要更新的时候，才去碰极其沉重的组件更新，避免单帧无限触发 stack.update
        ItemStack stack = this.getItem();
        if (!stack.isEmpty()) {
            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            boolean currentTag = customData.copyTag().getBoolean("IsAbsoluteHovering");

            if (currentTag != hovering) {
                stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, cd ->
                        cd.update(tag -> {
                            if (hovering) {
                                tag.putBoolean("IsAbsoluteHovering", true);
                            } else {
                                tag.remove("IsAbsoluteHovering");
                            }
                        })
                );
            }
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float damage) {
        // 放行原版的极限清除源（如 /kill 指令），防止世界崩溃时无法清除实体造成死档
        if (damageSource.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            // 如果已经在主世界出生点保护区域，免疫虚空，否则允许原版销毁作为防死锁机制
            return !isAtOverworldSpawn();
        }
        // 彻底免疫其余任何物理、雷劈、TNT爆炸、仙人掌伤害
        return false;
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        // 只有在出生点且处于悬浮状态，才锁死运动状态
        if (this.isAbsoluteHovering() && isAtOverworldSpawn()) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(true);
            this.hasImpulse = false;
        }
    }

    @Override
    public void tick() {
        // 🎯 修复：分离判定，如果脱离了主世界出生点，则解除悬浮标记，顺畅掉落
        if (this.isAbsoluteHovering()) {
            if (!isAtOverworldSpawn()) {
                this.setAbsoluteHovering(false);
            } else {
                // 真正的虚空拯救静止状态（在主世界出生点且有标记）
                this.setDeltaMovement(Vec3.ZERO);
                this.setNoGravity(true);
                this.hasImpulse = false;
                // 锁定位置历史，阻断客户端插值产生的画面疯狂上下抖动
                this.xo = this.getX();
                this.yo = this.getY();
                this.zo = this.getZ();
            }
        }

        super.tick();

        // 3. 确保在 super.tick() 原版物理结算后，强行将其运动状态焊死在 0
        if (this.isAbsoluteHovering() && isAtOverworldSpawn()) {
            this.setDeltaMovement(Vec3.ZERO);
            this.hasImpulse = false;
        }

        // 4. 定期保活，每秒强行刷新一次生存寿命，确保在没人捡时永远不会自然消失
        if (!this.level().isClientSide()) {
            if (this.tickCount % 20 == 0) {
                this.setUnlimitedLifetime();
            }
        }
    }

    private boolean isAtOverworldSpawn() {
        if (this.level().dimension() != Level.OVERWORLD) {
            return false;
        }
        BlockPos spawnPos = this.level().getSharedSpawnPos();
        double dx = Math.abs(this.getX() - (spawnPos.getX() + 0.5));
        double dy = Math.abs(this.getY() - (spawnPos.getY() + 3.0));
        double dz = Math.abs(this.getZ() - (spawnPos.getZ() + 0.5));
        return dx < 2.0 && dy < 5.0 && dz < 2.0;
    }

    /**
     * 检查实体是否低于世界边界，如果是则将其通过神秘学不灭定律跨次元拉回主世界
     */
    @Override
    public void checkBelowWorld() {
        // 🛡️ 如果已经在跨维度转场中，直接拦截，绝不允许重复执行触发死循环
        if (this.isTransferringDimension) {
            return;
        }

        if (this.getY() < (double)(this.level().getMinBuildHeight() - 20)) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel currentLevel) {
                net.minecraft.server.MinecraftServer server = currentLevel.getServer();
                ServerLevel overworld = server.getLevel(Level.OVERWORLD);

                if (overworld != null) {
                    BlockPos spawnPos = overworld.getSharedSpawnPos();
                    double targetX = spawnPos.getX() + 0.5;
                    double targetY = spawnPos.getY() + 3.0; // 出生点上方 3 格
                    double targetZ = spawnPos.getZ() + 0.5;

                    this.setAbsoluteHovering(true);
                    this.setDeltaMovement(Vec3.ZERO);

                    if (currentLevel.dimension() != Level.OVERWORLD) {
                        // 🔒 激活转场保护锁
                        this.isTransferringDimension = true;

                        this.changeDimension(new DimensionTransition(
                                overworld,
                                new Vec3(targetX, targetY, targetZ),
                                Vec3.ZERO,
                                this.getYRot(),
                                this.getXRot(),
                                // ✨ 优化：转场成功后解除锁定，并确保新实体完美继承物理悬浮状态
                                entity -> {
                                    if (entity instanceof IndestructibleItemEntity indestructible) {
                                        indestructible.isTransferringDimension = false;
                                        indestructible.setAbsoluteHovering(true);
                                        indestructible.setDeltaMovement(Vec3.ZERO);
                                    }
                                }
                        ));
                    } else {
                        this.teleportTo(targetX, targetY, targetZ);
                    }
                }
            }
        }
    }
}