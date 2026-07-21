package com.nightydead.lordofmysteries.skills;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * 结构占卜处理器 — 搜索最近的目标结构位置并生成粒子指引
 */
public class StructureDivinationHandler {

    private static final double TRAIL_SPACING = 0.3;
    private static final Vector3f PARTICLE_COLOR = new Vector3f(1.0f, 0.84f, 0.2f);
    private static final float PARTICLE_SIZE = 1.4f;

    /**
     * 在玩家周围搜索最近的目标结构位置
     *
     * @param player      执行占卜的玩家
     * @param structureId 目标结构的 ResourceLocation
     * @param radius      搜索半径（方块）
     * @return 最近的结构起始坐标，未找到返回 null
     */
    public static BlockPos findNearestStructure(Player player, ResourceLocation structureId, int radius) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return null;

        var structureRegistry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
        ResourceKey<Structure> targetKey = ResourceKey.create(Registries.STRUCTURE, structureId);
        var holderOpt = structureRegistry.getHolder(targetKey);

        if (holderOpt.isEmpty()) return null;

        HolderSet<Structure> holderSet = HolderSet.direct(holderOpt.get());
        Pair<BlockPos, Holder<Structure>> result = serverLevel.getChunkSource().getGenerator()
                .findNearestMapStructure(serverLevel, holderSet, player.blockPosition(), radius, false);

        return result != null ? result.getFirst() : null;
    }

    public static void spawnGuidanceTrail(ServerLevel level, Player player, BlockPos target) {
        spawnGuidanceTrail(level, player.getEyePosition(), target);
    }

    public static void spawnGuidanceTrail(ServerLevel level, Vec3 start, BlockPos target) {
        Vec3 end = Vec3.atCenterOf(target);
        Vec3 dir = end.subtract(start);
        double totalDist = dir.length();

        var particle = new DustParticleOptions(PARTICLE_COLOR, PARTICLE_SIZE);

        for (double d = 0.3; d < totalDist; d += TRAIL_SPACING) {
            Vec3 pos = start.add(dir.normalize().scale(d));
            level.sendParticles(particle,
                    pos.x, pos.y, pos.z,
                    1, 0.02, 0.02, 0.02, 0.01);
        }

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
