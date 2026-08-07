package com.nightydead.lordofmysteries.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.material.FluidState;

/**
 * 拉瓦章鱼（Lava Octopus）
 * 继承荧光鱿鱼，生活在下方岩浆湖中。
 * 使用荧光鱿鱼的模型和AI，但免疫火焰并在岩浆中游泳。
 * 仅在大型岩浆湖中生成。
 */
@SuppressWarnings("deprecation")
public class LavaOctopusEntity extends GlowSquid {

    /** 岩浆湖最小规模阈值：检查范围内至少需要的岩浆方块数量 */
    private static final int MIN_LAVA_BLOCK_COUNT = 35;
    /** 水平检测范围半径（3x3, 5x5, 7x7 嵌套检查） */
    private static final int CHECK_RADIUS = 3;

    public LavaOctopusEntity(EntityType<? extends GlowSquid> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * 在岩浆中也视为"在水中"，使鱿鱼游泳AI正常工作
     * 覆盖 isInWaterOrBubble 而非已弃用的 isInWater
     * 使用 FluidTags.LAVA 直接检测流体状态，避免已弃用的 isInLava()
     */
    @Override
    public boolean isInWaterOrBubble() {
        return super.isInWaterOrBubble() || this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA);
    }

    /**
     * 免疫火焰伤害（在岩浆中不受伤害）
     */
    @Override
    public boolean fireImmune() {
        return true;
    }

    /**
     * 每 tick 在岩浆中产生气泡粒子（覆盖荧光鱿鱼的发光粒子效果，
     * 改为冒泡粒子模拟岩浆气泡）
     */
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA)) {
            // 在岩浆中生成岩浆气泡粒子
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(
                    ParticleTypes.LAVA,
                    this.getRandomX(0.5),
                    this.getRandomY(),
                    this.getRandomZ(0.5),
                    0.0, 0.0, 0.0
                );
            }
        }
    }

    /**
     * 生成规则检测：在岩浆中 + 岩浆范围足够大
     */
    public static boolean checkLavaOctopusSpawnRules(
        EntityType<? extends LivingEntity> lavaOctopus,
        ServerLevelAccessor level,
        MobSpawnType spawnType,
        BlockPos pos,
        RandomSource random
    ) {
        // 基础检测：目标位置是岩浆
        FluidState fluidState = level.getFluidState(pos);
        if (!fluidState.is(FluidTags.LAVA)) {
            return false;
        }
        // 上方必须是自由空间（岩浆/空气，不能是固体）
        BlockPos abovePos = pos.above();
        if (level.getBlockState(abovePos).isSolid()) {
            return false;
        }
        // 岩浆范围大小检测：7x7 水平范围内的岩浆方块数量至少达到阈值
        int lavaCount = 0;
        for (int dx = -CHECK_RADIUS; dx <= CHECK_RADIUS; dx++) {
            for (int dz = -CHECK_RADIUS; dz <= CHECK_RADIUS; dz++) {
                BlockPos checkPos = new BlockPos(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
                if (level.getFluidState(checkPos).is(FluidTags.LAVA)) {
                    lavaCount++;
                }
            }
        }
        return lavaCount >= MIN_LAVA_BLOCK_COUNT;
    }
}
