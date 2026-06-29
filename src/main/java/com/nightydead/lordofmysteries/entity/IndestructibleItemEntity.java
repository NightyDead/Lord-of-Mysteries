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

/**
 * 不可破坏的物品实体类，继承自ItemEntity
 * 该实体具有不可破坏、无限生命周期的特性，并支持特殊的悬浮行为
 */
public class IndestructibleItemEntity extends ItemEntity {

    /**
     * 构造方法1：使用EntityType和Level创建实例
     * @param type 实体类型
     * @param level 所在游戏世界
     */
    public IndestructibleItemEntity(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        this.setInvulnerable(true);  // 设置为不可破坏
        this.setUnlimitedLifetime(); // 设置无限生命周期
    }

    /**
     * 构造方法2：使用坐标和物品堆栈创建实例
     * @param level 所在游戏世界
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     * @param stack 物品堆栈
     */
    public IndestructibleItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(level, x, y, z, stack);
        this.setInvulnerable(true);  // 设置为不可破坏
        this.setUnlimitedLifetime(); // 设置无限生命周期
    }

    /**
     * 检查物品是否处于绝对悬浮状态
     * @return 如果物品具有绝对悬浮标记则返回true，否则返回false
     */
    public boolean isAbsoluteHovering() {
        ItemStack stack = this.getItem();
        if (stack.isEmpty()) return false;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag().getBoolean("IsAbsoluteHovering");
        }
        return false;
    }

    /**
     * 设置物品的绝对悬浮状态
     * @param hovering 是否启用绝对悬浮
     */
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

    /**
     * 重写伤害方法，使实体无法受到伤害
     * @param damageSource 伤害源
     * @param damage 伤害值
     * @return 总是返回false，表示不受到伤害
     */
    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float damage) {
        return false;
    }

    /**
     * 当同步数据更新时的处理
     * 如果实体处于绝对悬浮状态且在主世界出生点，则使其保持静止
     * @param key 数据访问器
     */
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

    /**
     * 每tick更新时的处理
     * 处理实体的特殊悬浮行为，并在服务端定期更新无限生命周期
     */
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
    /**
     * 检查实体是否低于世界边界，如果是则将其传送到出生点
     * 这个方法通常用于防止玩家或实体掉出世界底部
     */
    public void checkBelowWorld() {
        // 检查实体当前的Y坐标是否低于世界最小建造高度
        if (this.getY() < (double)(this.level().getMinBuildHeight())) {
            // 确保在服务器端执行，并且当前世界是ServerLevel实例
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel currentLevel) {
                // 获取当前服务器实例
                net.minecraft.server.MinecraftServer server = currentLevel.getServer();
                // 获取主世界(Overworld)的引用
                ServerLevel overworld = server.getLevel(Level.OVERWORLD);

                // 确保主世界存在
                if (overworld != null) {
                    // 获取主世界的共享出生点位置
                    BlockPos spawnPos = overworld.getSharedSpawnPos();

                    // 计算目标坐标，出生点上方3格
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