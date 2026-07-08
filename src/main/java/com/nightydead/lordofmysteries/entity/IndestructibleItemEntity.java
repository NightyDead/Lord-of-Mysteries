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

    /** 是否正在进行维度转移的标志位，防止重复触发 */
    private boolean isTransferringDimension = false;
    /** 运行时悬浮状态标志 */
    private boolean isHoveringRuntime = false;
    /** 悬浮状态是否已从 NBT 初始化 */
    private boolean isHoveringInitialized = false;

    /**
     * 构造不可破坏物品实体（通过 EntityType 创建）
     *
     * @param type  实体类型
     * @param level 所在世界
     */
    public IndestructibleItemEntity(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        initIndestructible();
    }

    /**
     * 构造不可破坏物品实体（通过坐标创建）
     *
     * @param level 所在世界
     * @param x     X 坐标
     * @param y     Y 坐标
     * @param z     Z 坐标
     * @param stack 携带的物品堆栈
     */
    public IndestructibleItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(level, x, y, z, stack);
        initIndestructible();
    }

    /** 初始化不可破坏属性：设置无敌状态和无限生命周期 */
    private void initIndestructible() {
        this.setInvulnerable(true);
        this.setUnlimitedLifetime();
    }

    /**
     * 获取实体的绝对悬浮状态
     * 首次调用时从物品 NBT 中读取悬浮标志并缓存
     *
     * @return 是否处于悬浮状态
     */
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

    /**
     * 设置实体的悬浮状态
     * 同时更新运行时标志、重力状态和物品 NBT 数据
     *
     * @param hovering 是否启用悬浮
     */
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

    /** 检测实体是否位于主世界出生点附近（用于判断是否应悬浮） */
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