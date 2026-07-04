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
 * 不可破坏的物品实体类 - 完美契合神秘学非凡特性不灭定律
 * 核心功能：绝对无敌、防虚空湮灭、主世界出生点自动悬浮打捞
 */
public class IndestructibleItemEntity extends ItemEntity {

    private boolean isTransferringDimension = false;
    private boolean isHoveringRuntime = false;
    private boolean isHoveringInitialized = false;

    public IndestructibleItemEntity(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        initIndestructible();
    }

    public IndestructibleItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(level, x, y, z, stack);
        initIndestructible();
    }

    private void initIndestructible() {
        this.setInvulnerable(true);
        this.setUnlimitedLifetime();
    }

    public boolean isAbsoluteHovering() {
        if (this.isHoveringInitialized) return this.isHoveringRuntime;

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

        ItemStack stack = this.getItem();
        if (!stack.isEmpty()) {
            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            if (customData.copyTag().getBoolean("IsAbsoluteHovering") != hovering) {
                stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, cd ->
                        cd.update(tag -> {
                            if (hovering) tag.putBoolean("IsAbsoluteHovering", true);
                            else tag.remove("IsAbsoluteHovering");
                        })
                );
            }
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float damage) {
        // 仅在脱离出生点时放行虚空伤害作为防死锁兜底，其余物理/雷劈伤害完全免疫
        return damageSource.is(DamageTypes.FELL_OUT_OF_WORLD) && !isAtOverworldSpawn();
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (this.isAbsoluteHovering() && isAtOverworldSpawn()) {
            lockPhysicalState();
        }
    }

    @Override
    public void tick() {
        if (this.isAbsoluteHovering()) {
            if (!isAtOverworldSpawn()) {
                this.setAbsoluteHovering(false);
            } else {
                lockPhysicalState();
            }
        }

        super.tick();

        if (this.isAbsoluteHovering() && isAtOverworldSpawn()) {
            this.setDeltaMovement(Vec3.ZERO);
            this.hasImpulse = false;
        }

        if (!this.level().isClientSide() && this.tickCount % 20 == 0) {
            this.setUnlimitedLifetime(); // 定期保活不灭
        }
    }

    /**
     * 将悬浮实体的移动向量与插值历史焊死在原地，阻断客户端抖动
     */
    private void lockPhysicalState() {
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        this.hasImpulse = false;
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
    }

    private boolean isAtOverworldSpawn() {
        if (this.level().dimension() != Level.OVERWORLD) return false;
        BlockPos spawnPos = this.level().getSharedSpawnPos();
        return Math.abs(this.getX() - (spawnPos.getX() + 0.5)) < 2.0
                && Math.abs(this.getY() - (spawnPos.getY() + 3.0)) < 5.0
                && Math.abs(this.getZ() - (spawnPos.getZ() + 0.5)) < 2.0;
    }

    @Override
    public void checkBelowWorld() {
        if (this.isTransferringDimension || this.getY() >= (double)(this.level().getMinBuildHeight() - 20)) return;

        if (!this.level().isClientSide() && this.level() instanceof ServerLevel currentLevel) {
            ServerLevel overworld = currentLevel.getServer().getLevel(Level.OVERWORLD);
            if (overworld != null) {
                BlockPos spawnPos = overworld.getSharedSpawnPos();
                double tx = spawnPos.getX() + 0.5, ty = spawnPos.getY() + 3.0, tz = spawnPos.getZ() + 0.5;

                this.setAbsoluteHovering(true);
                this.setDeltaMovement(Vec3.ZERO);

                if (currentLevel.dimension() != Level.OVERWORLD) {
                    this.isTransferringDimension = true;
                    this.changeDimension(new DimensionTransition(
                            overworld, new Vec3(tx, ty, tz), Vec3.ZERO, this.getYRot(), this.getXRot(),
                            entity -> {
                                if (entity instanceof IndestructibleItemEntity indestructible) {
                                    indestructible.isTransferringDimension = false;
                                    indestructible.setAbsoluteHovering(true);
                                    indestructible.setDeltaMovement(Vec3.ZERO);
                                }
                            }
                    ));
                } else {
                    this.teleportTo(tx, ty, tz);
                }
            }
        }
    }
}