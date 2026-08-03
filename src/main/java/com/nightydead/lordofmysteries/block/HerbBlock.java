package com.nightydead.lordofmysteries.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 模组草药方块类
 * 继承原版花朵行为（可种花盆、可合成等），并额外支持骨粉繁殖：
 * 对植株使用骨粉时，在其周围 3×3 区域内随机扩散长出同种草药
 */
public class HerbBlock extends FlowerBlock implements BonemealableBlock {

    /** 触碰时施加的状态效果持有者 */
    private final Holder<MobEffect> effect;

    /** 状态效果持续时长（tick），由构造参数秒数换算 */
    private final int effectDuration;

    /**
     * 构造草药方块
     *
     * @param effect         触碰/食用时施加的状态效果持有者
     * @param effectDuration 效果持续时长（秒）
     * @param properties     方块属性
     */
    public HerbBlock(Holder<MobEffect> effect, float effectDuration, Properties properties) {
        super(effect, effectDuration, properties);
        this.effect = effect;
        this.effectDuration = (int) (effectDuration * 20.0F);
    }

    /**
     * 触碰效果：对进入方块的非蜜蜂生物施加状态效果（与黑边太阳花行为一致）
     */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity livingEntity && entity.getType() != EntityType.BEE) {
            livingEntity.addEffect(new MobEffectInstance(this.effect, this.effectDuration));
        }
    }

    /**
     * 骨粉目标判定：草药植物始终允许被骨粉催熟繁殖
     */
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    /**
     * 骨粉成功率：始终成功（具体繁殖数量由 performBonemeal 内随机决定）
     */
    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    /**
     * 骨粉繁殖：在植株周围 3×3 水平范围（含上下 1 格）内随机扩散 1~3 株同种草药
     * 仅在目标位置为空气且满足生存条件（下方为泥土/沙子等）时放置
     */
    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int count = 1 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            BlockPos target = pos.offset(
                    random.nextInt(3) - 1,
                    random.nextInt(2) - 1,
                    random.nextInt(3) - 1
            );
            if (target.equals(pos)) {
                continue;
            }
            BlockState current = level.getBlockState(target);
            if (current.isAir() && defaultBlockState().canSurvive(level, target)) {
                level.setBlock(target, defaultBlockState(), 3);
            }
        }
    }
}
