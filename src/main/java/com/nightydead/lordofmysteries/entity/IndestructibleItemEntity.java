package com.nightydead.lordofmysteries.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class IndestructibleItemEntity extends ItemEntity {

    // 1. 系统/存档加载使用的构造函数
    public IndestructibleItemEntity(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        this.setInvulnerable(true); // 基础无敌状态
        this.setUnlimitedLifetime();
    }

    // 2. 玩家扔出物品时使用的构造函数
    public IndestructibleItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(level, x, y, z, stack);
        this.setInvulnerable(true);
        this.setUnlimitedLifetime();
        this.setAbsoluteHovering(false);
    }

    /*
     * 【核心修复】利用 1.21.1 的 DataComponent 检查物品堆内是否带有神性悬浮标记
     * 这样不论是在客户端还是服务端，读档还是网络同步，全部由原版组件机制完美代劳，永远不会报索引越界崩溃
     */
    public boolean isAbsoluteHovering() {
        ItemStack stack = this.getItem();
        if (stack.isEmpty()) return false;

        // 检查物品堆的自定义 NBT 组件中是否含有 "IsAbsoluteHovering" 标签
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag().getBoolean("IsAbsoluteHovering");
        }
        return false;
    }

    /*
     * 【核心修复】设置绝对悬浮标记
     */
    public void setAbsoluteHovering(boolean hovering) {
        ItemStack stack = this.getItem();
        if (!stack.isEmpty()) {
            // 通过 1.21.1 的组件机制修改物品的自定义数据
            stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, customData ->
                    customData.update(tag -> {
                        if (hovering) {
                            tag.putBoolean("IsAbsoluteHovering", true);
                        } else {
                            tag.remove("IsAbsoluteHovering");
                        }
                    })
            );
            // 顺便更新原版的无重力物理状态
            this.setNoGravity(hovering);
        }
    }

    /*
     * 3. 免疫一切常规伤害
     */
    @Override
    public boolean hurt(DamageSource damageSource, float damage) {
        return false;
    }

    /*
     * 4. 彻底免疫虚空（无论在哪个维度掉落，一律跨界聚合，悬浮于主世界出生点）
     */
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

                    // 激活悬浮状态：直接在ItemStack里刻下标记，网络数据打包会自动同步给客户端
                    this.setAbsoluteHovering(true);
                    this.setDeltaMovement(0, 0, 0);
                }
            }
        }
    }

    @Override
    public void tick() {
        // 在 super.tick() 走原版重力物理（applyGravity）前进行强行拦截
        if (this.isAbsoluteHovering()) {
            this.setNoGravity(true);
            this.setDeltaMovement(0, 0, 0);
        }

        super.tick();

        if (!this.level().isClientSide()) {
            // 每秒执行一次保险
            if (this.tickCount % 20 == 0) {
                this.setUnlimitedLifetime(); // 续命

                if (this.isAbsoluteHovering()) {
                    this.setDeltaMovement(0, 0, 0);
                    // 强行把它的物理运动锁死
                    if (!this.isNoGravity()) {
                        this.setNoGravity(true);
                    }
                }
            }
        }
    }
}