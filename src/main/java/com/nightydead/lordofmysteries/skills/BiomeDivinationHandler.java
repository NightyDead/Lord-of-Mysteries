package com.nightydead.lordofmysteries.skills;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * 群系占卜处理器 — 搜索最近的目标群系位置并生成粒子指引
 */
public class BiomeDivinationHandler {

    /** 粒子轨迹间距（方块） */
    private static final double TRAIL_SPACING = 0.3;
    /** 粒子颜色 — 灵性金色 */
    private static final Vector3f PARTICLE_COLOR = new Vector3f(1.0f, 0.84f, 0.2f);
    /** 粒子大小 */
    private static final float PARTICLE_SIZE = 1.4f;

    /**
     * 在玩家周围搜索最近的目标群系位置
     *
     * @param player   执行占卜的玩家
     * @param biomeId  目标群系的 ResourceLocation（如 "minecraft:plains"）
     * @param radius   搜索半径（方块）
     * @param step     搜索步长（方块），值越大性能越好但精度越低
     * @return 最近的群系方块坐标，未找到返回 null
     */
    public static BlockPos findNearestBiome(Player player, ResourceLocation biomeId,
                                             int radius, int step) {
        BlockPos center = player.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        // 构建目标群系的 ResourceKey
        ResourceKey<Biome> targetKey = ResourceKey.create(Registries.BIOME, biomeId);

        for (int dx = -radius; dx <= radius; dx += step) {
            for (int dy = -radius; dy <= radius; dy += step) {
                for (int dz = -radius; dz <= radius; dz += step) {
                    // 球形裁剪
                    if (dx * dx + dy * dy + dz * dz > radius * radius) continue;

                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);

                    // 检查该位置的群系
                    var biomeHolder = player.level().getBiome(cursor);
                    if (biomeHolder.is(targetKey)) {
                        double distSq = cursor.distSqr(center);
                        if (distSq < nearestDistSq) {
                            nearestDistSq = distSq;
                            nearest = cursor.immutable();
                        }
                    }
                }
            }
        }

        return nearest;
    }

    /**
     * 生成粒子轨迹 — 从玩家眼部到目标群系中心的金色粒子链
     *
     * @param level  服务端世界
     * @param player 占卜玩家
     * @param target 目标坐标
     */
    public static void spawnGuidanceTrail(ServerLevel level, Player player, BlockPos target) {
        spawnGuidanceTrail(level, player.getEyePosition(), target);
    }

    /**
     * 生成粒子轨迹 — 使用固定起点，用于延时波次避免玩家移动导致轨迹偏移
     *
     * @param level  服务端世界
     * @param start  固定的轨迹起点
     * @param target 目标坐标
     */
    public static void spawnGuidanceTrail(ServerLevel level, Vec3 start, BlockPos target) {
        Vec3 end = Vec3.atCenterOf(target);
        Vec3 dir = end.subtract(start);
        double totalDist = dir.length();
        Vec3 step = dir.normalize().scale(TRAIL_SPACING);

        var particle = new DustParticleOptions(PARTICLE_COLOR, PARTICLE_SIZE);

        // 沿路径均匀生成粒子
        for (double d = 0.3; d < totalDist; d += TRAIL_SPACING) {
            Vec3 pos = start.add(dir.normalize().scale(d));
            level.sendParticles(particle,
                    pos.x, pos.y, pos.z,
                    1, 0.02, 0.02, 0.02, 0.01);
        }

        // 目标位置处额外生成多圈标记粒子（双层环 + 顶部标记）
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI * 2 / 12;
            double ox = Math.cos(angle) * 0.4;
            double oz = Math.sin(angle) * 0.4;
            level.sendParticles(particle,
                    end.x + ox, end.y + 0.3, end.z + oz,
                    1, 0, 0, 0, 0.02);
            level.sendParticles(particle,
                    end.x + ox, end.y + 0.8, end.z + oz,
                    1, 0, 0, 0, 0.02);
            level.sendParticles(particle,
                    end.x + ox, end.y + 1.3, end.z + oz,
                    1, 0, 0, 0, 0.02);
        }
    }
}
