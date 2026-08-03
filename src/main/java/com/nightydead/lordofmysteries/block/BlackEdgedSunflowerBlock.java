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
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 黑边太阳花 - 小丑魔药辅材来源
 * <p>
 * 与原版向日葵一致的双格植物：下半格为茎叶，上半格为带黑边的花头。
 * 触碰时对生物施加状态效果（与原 HerbBlock 草药行为一致），并支持骨粉繁殖扩散。
 * 放置下半格后由 {@link DoublePlantBlock#onPlace} 自动补齐上半格。
 */
public class BlackEdgedSunflowerBlock extends DoublePlantBlock implements BonemealableBlock {

    /** 触碰时施加的状态效果持有者 */
    private final Holder<MobEffect> effect;

    /** 状态效果持续时长（tick），由构造参数秒数换算 */
    private final int effectDuration;

    /**
     * 构造黑边太阳花方块
     *
     * @param effect              触碰时施加的状态效果持有者
     * @param effectDurationSeconds 效果持续时长（秒）
     * @param properties          方块属性
     */
    public BlackEdgedSunflowerBlock(Holder<MobEffect> effect, float effectDurationSeconds, Properties properties) {
        super(properties);
        this.effect = effect;
        this.effectDuration = (int) (effectDurationSeconds * 20.0F);
    }

    /**
     * 触碰效果：与 FlowerBlock 一致——对进入方块的非蜜蜂生物施加状态效果
     */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity livingEntity && entity.getType() != EntityType.BEE) {
            livingEntity.addEffect(new MobEffectInstance(this.effect, this.effectDuration));
        }
    }

    /**
     * 骨粉目标判定：始终允许被骨粉催熟繁殖
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
     * 双格植物要求目标位置与上方一格均为空气且满足生存条件，
     * 放置下半格后由 DoublePlantBlock 的 onPlace 自动补齐上半格
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
            if (target.equals(pos)
                    || !level.getBlockState(target).isAir()
                    || !level.getBlockState(target.above()).isAir()) {
                continue;
            }
            if (defaultBlockState().canSurvive(level, target)) {
                level.setBlock(target, defaultBlockState(), 3);
            }
        }
    }
}
