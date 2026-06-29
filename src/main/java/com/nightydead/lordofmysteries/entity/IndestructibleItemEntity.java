package com.nightydead.lordofmysteries.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class IndestructibleItemEntity extends ItemEntity {

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

    public boolean isAbsoluteHovering() {
        ItemStack stack = this.getItem();
        if (stack.isEmpty()) return false;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag().getBoolean("IsAbsoluteHovering");
        }
        return false;
    }

    public void setAbsoluteHovering(boolean hovering) {
        ItemStack stack = this.getItem();
        if (!stack.isEmpty()) {
            stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, customData ->
                    customData.update(tag -> {
                        if (hovering) {
                            tag.putBoolean("IsAbsoluteHovering", true);
                        } else {
                            tag.remove("IsAbsoluteHovering");
                        }
                    })
            );
        }
        this.setNoGravity(hovering);
    }

    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float damage) {
        return false;
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        // 数据同步时，如果满足真实的悬浮环境，立刻锁死
        if (this.isAbsoluteHovering() && isAtOverworldSpawn()) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(true);
            this.hasImpulse = false;
        }
    }

    @Override
    public void tick() {
        // 核心修复：如果是玩家扔出来的（带标记，但是不在主世界出生点），立刻彻底擦除标记，让其正常下落
        if (this.isAbsoluteHovering() && !isAtOverworldSpawn()) {
            this.setAbsoluteHovering(false);
            this.setNoGravity(false);
        }

        // 真正的虚空拯救静止状态（在主世界出生点且有标记）
        if (this.isAbsoluteHovering()) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(true);
            this.hasImpulse = false;
            this.xo = this.getX();
            this.yo = this.getY();
            this.zo = this.getZ();
        }

        super.tick();

        if (this.isAbsoluteHovering()) {
            this.setDeltaMovement(Vec3.ZERO);
            this.hasImpulse = false;
        }

        if (!this.level().isClientSide()) {
            if (this.tickCount % 20 == 0) {
                this.setUnlimitedLifetime();
            }
        }
    }

    /**
     * 【核心辅助方法】判断当前实体是否处于主世界的出生点聚合范围内
     * 无论客户端还是服务端都可以安全调用
     */
    private boolean isAtOverworldSpawn() {
        // 1. 维度必须是主世界
        if (this.level().dimension() != Level.OVERWORLD) {
            return false;
        }

        // 2. 获取当前维度的出生点坐标
        BlockPos spawnPos = this.level().getSharedSpawnPos();

        // 3. 检查实体的坐标是否在出生点 X, Z 轴半径 2 格、Y 轴上下 5 格的范围内
        double dx = Math.abs(this.getX() - (spawnPos.getX() + 0.5));
        double dy = Math.abs(this.getY() - (spawnPos.getY() + 3.0));
        double dz = Math.abs(this.getZ() - (spawnPos.getZ() + 0.5));

        return dx < 2.0 && dy < 5.0 && dz < 2.0;
    }

    @Override
    public void checkBelowWorld() {
        if (this.getY() < (double)(this.level().getMinBuildHeight())) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel currentLevel) {
                net.minecraft.server.MinecraftServer server = currentLevel.getServer();
                ServerLevel overworld = server.getLevel(Level.OVERWORLD);

                if (overworld != null) {
                    BlockPos spawnPos = overworld.getSharedSpawnPos();

                    double targetX = spawnPos.getX() + 0.5;
                    double targetY = spawnPos.getY() + 3.0; // 出生点上方 3 格
                    double targetZ = spawnPos.getZ() + 0.5;

                    // 激活标记
                    this.setAbsoluteHovering(true);
                    this.setDeltaMovement(Vec3.ZERO);

                    if (currentLevel.dimension() != Level.OVERWORLD) {
                        this.changeDimension(new net.minecraft.world.level.portal.DimensionTransition(
                                overworld,
                                new net.minecraft.world.phys.Vec3(targetX, targetY, targetZ),
                                net.minecraft.world.phys.Vec3.ZERO,
                                this.getYRot(),
                                this.getXRot(),
                                net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING
                        ));
                    } else {
                        this.teleportTo(targetX, targetY, targetZ);
                    }
                }
            }
        }
    }
}