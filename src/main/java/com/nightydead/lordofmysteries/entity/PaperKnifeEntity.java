package com.nightydead.lordofmysteries.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 「化纸为刀」- 纸刀抛射物实体
 * <p>
 * 将纸化作飞刀直线掷出：无重力、无下坠，速度 1.5 格/tick，
 * 最长飞行 20 tick（共 30 格攻击范围），命中生物造成 5 点伤害，
 * 无论是否命中目标，纸刀都会消失且不可回收
 */
public class PaperKnifeEntity extends ThrowableProjectile implements ItemSupplier {

    /** 飞行速度（格/tick），1.5 × 20 tick = 30 格攻击范围 */
    private static final float SPEED = 1.5F;
    /** 最大飞行时长（tick），对应 30 格射程 */
    private static final int MAX_LIFETIME = 20;
    /** 命中伤害 */
    private static final float DAMAGE = 5.0F;

    public PaperKnifeEntity(EntityType<? extends PaperKnifeEntity> type, Level level) {
        super(type, level);
    }

    public PaperKnifeEntity(EntityType<? extends PaperKnifeEntity> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    /** 渲染用的物品：一张纸 */
    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.PAPER);
    }

    /** 纸刀无需同步实体数据 */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    /** 无重力：直线飞行，没有下坠 */
    @Override
    protected double getDefaultGravity() {
        return 0.0D;
    }

    /**
     * 完全自定义飞行逻辑（不调用 super.tick()，绕开默认重力与命中检测）：
     * <ul>
     *   <li>超过 20 tick（30 格）立即消失</li>
     *   <li>服务端：实体命中 → 5 点伤害后消失；方块命中 → 消失；否则继续直线前进</li>
     *   <li>客户端：本地同步移动（最终位置由服务端插值覆盖）</li>
     * </ul>
     */
    @Override
    public void tick() {
        this.baseTick();

        if (this.tickCount > MAX_LIFETIME) {
            this.discard();
            return;
        }

        Vec3 vel = this.getDeltaMovement();
        if (this.level().isClientSide) {
            this.setPos(this.getX() + vel.x, this.getY() + vel.y, this.getZ() + vel.z);
            return;
        }

        Vec3 pos = this.position();
        Vec3 next = pos.add(vel);

        // 实体命中检测（排除发射者本人）
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                this, pos, next,
                this.getBoundingBox().expandTowards(vel).inflate(1.0),
                this::canHitEntity, 30.0D);
        if (entityHit != null) {
            this.onHit(entityHit);
            return;
        }

        // 方块命中检测（撞墙即消失）
        BlockHitResult blockHit = this.level().clip(
                new ClipContext(pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHit.getType() != HitResult.Type.MISS) {
            this.onHitBlock(blockHit);
            return;
        }

        this.setPos(next.x, next.y, next.z);
    }

    /** 命中生物：造成 5 点抛射物伤害，纸刀消失（不可回收） */
    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        target.hurt(this.damageSources().thrown(this, owner), DAMAGE);
        this.discard();
    }

    /** 命中方块：纸刀撞墙消失（不可回收） */
    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.discard();
    }

    /** 不可命中发射者本人 */
    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != this.getOwner();
    }
}
