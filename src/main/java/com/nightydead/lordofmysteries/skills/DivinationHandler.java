package com.nightydead.lordofmysteries.skills;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.*;

/**
 * 占卜技能处理器 — 矿物占卜
 * <p>
 * 玩家手持矿物类物品（矿石、粗矿、矿石块、锭、粒）时触发占卜，
 * 在 32 格半径内搜索对应的原矿，生成金色粒子轨迹指引方向
 */
public class DivinationHandler {

    /** 占卜搜索半径（方块） */
    public static final int SEARCH_RADIUS = 32;
    /** 粒子轨迹间距（方块） */
    private static final double TRAIL_SPACING = 0.6;
    /** 粒子颜色 — 灵性金色 */
    private static final Vector3f PARTICLE_COLOR = new Vector3f(1.0f, 0.84f, 0.2f);
    /** 粒子大小 */
    private static final float PARTICLE_SIZE = 1.4f;

    /** 矿物物品 → 目标矿石块的映射表 */
    private static final Map<Item, List<Block>> MINERAL_TO_ORES = new HashMap<>();

    static {
        // ==================== 煤炭 ====================
        List<Block> coalOres = List.of(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE);
        MINERAL_TO_ORES.put(Items.COAL, coalOres);
        MINERAL_TO_ORES.put(Items.COAL_ORE, coalOres);
        MINERAL_TO_ORES.put(Items.DEEPSLATE_COAL_ORE, coalOres);
        MINERAL_TO_ORES.put(Items.COAL_BLOCK, coalOres);

        // ==================== 铁 ====================
        List<Block> ironOres = List.of(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE);
        MINERAL_TO_ORES.put(Items.IRON_INGOT, ironOres);
        MINERAL_TO_ORES.put(Items.RAW_IRON, ironOres);
        MINERAL_TO_ORES.put(Items.IRON_ORE, ironOres);
        MINERAL_TO_ORES.put(Items.DEEPSLATE_IRON_ORE, ironOres);
        MINERAL_TO_ORES.put(Items.IRON_BLOCK, ironOres);
        MINERAL_TO_ORES.put(Items.IRON_NUGGET, ironOres);

        // ==================== 铜 ====================
        List<Block> copperOres = List.of(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE);
        MINERAL_TO_ORES.put(Items.COPPER_INGOT, copperOres);
        MINERAL_TO_ORES.put(Items.RAW_COPPER, copperOres);
        MINERAL_TO_ORES.put(Items.COPPER_ORE, copperOres);
        MINERAL_TO_ORES.put(Items.DEEPSLATE_COPPER_ORE, copperOres);
        MINERAL_TO_ORES.put(Items.COPPER_BLOCK, copperOres);

        // ==================== 金 ====================
        List<Block> goldOres = List.of(Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE);
        MINERAL_TO_ORES.put(Items.GOLD_INGOT, goldOres);
        MINERAL_TO_ORES.put(Items.RAW_GOLD, goldOres);
        MINERAL_TO_ORES.put(Items.GOLD_ORE, goldOres);
        MINERAL_TO_ORES.put(Items.DEEPSLATE_GOLD_ORE, goldOres);
        MINERAL_TO_ORES.put(Items.NETHER_GOLD_ORE, goldOres);
        MINERAL_TO_ORES.put(Items.GOLD_BLOCK, goldOres);
        MINERAL_TO_ORES.put(Items.GOLD_NUGGET, goldOres);

        // ==================== 红石 ====================
        List<Block> redstoneOres = List.of(Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);
        MINERAL_TO_ORES.put(Items.REDSTONE, redstoneOres);
        MINERAL_TO_ORES.put(Items.REDSTONE_ORE, redstoneOres);
        MINERAL_TO_ORES.put(Items.DEEPSLATE_REDSTONE_ORE, redstoneOres);
        MINERAL_TO_ORES.put(Items.REDSTONE_BLOCK, redstoneOres);

        // ==================== 青金石 ====================
        List<Block> lapisOres = List.of(Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE);
        MINERAL_TO_ORES.put(Items.LAPIS_LAZULI, lapisOres);
        MINERAL_TO_ORES.put(Items.LAPIS_ORE, lapisOres);
        MINERAL_TO_ORES.put(Items.DEEPSLATE_LAPIS_ORE, lapisOres);
        MINERAL_TO_ORES.put(Items.LAPIS_BLOCK, lapisOres);

        // ==================== 钻石 ====================
        List<Block> diamondOres = List.of(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);
        MINERAL_TO_ORES.put(Items.DIAMOND, diamondOres);
        MINERAL_TO_ORES.put(Items.DIAMOND_ORE, diamondOres);
        MINERAL_TO_ORES.put(Items.DEEPSLATE_DIAMOND_ORE, diamondOres);
        MINERAL_TO_ORES.put(Items.DIAMOND_BLOCK, diamondOres);

        // ==================== 绿宝石 ====================
        List<Block> emeraldOres = List.of(Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE);
        MINERAL_TO_ORES.put(Items.EMERALD, emeraldOres);
        MINERAL_TO_ORES.put(Items.EMERALD_ORE, emeraldOres);
        MINERAL_TO_ORES.put(Items.DEEPSLATE_EMERALD_ORE, emeraldOres);
        MINERAL_TO_ORES.put(Items.EMERALD_BLOCK, emeraldOres);

        // ==================== 下界石英 ====================
        List<Block> quartzOres = List.of(Blocks.NETHER_QUARTZ_ORE);
        MINERAL_TO_ORES.put(Items.QUARTZ, quartzOres);
        MINERAL_TO_ORES.put(Items.NETHER_QUARTZ_ORE, quartzOres);
        MINERAL_TO_ORES.put(Items.QUARTZ_BLOCK, quartzOres);

        // ==================== 远古残骸（下界合金） ====================
        List<Block> netheriteOres = List.of(Blocks.ANCIENT_DEBRIS);
        MINERAL_TO_ORES.put(Items.NETHERITE_INGOT, netheriteOres);
        MINERAL_TO_ORES.put(Items.NETHERITE_SCRAP, netheriteOres);
        MINERAL_TO_ORES.put(Items.ANCIENT_DEBRIS, netheriteOres);
        MINERAL_TO_ORES.put(Items.NETHERITE_BLOCK, netheriteOres);
    }

    /**
     * 获取手持物品对应的目标矿石类型列表
     *
     * @param item 玩家手持的物品
     * @return 对应的矿石方块列表，不匹配则返回 null
     */
    public static List<Block> getOreTargets(Item item) {
        return MINERAL_TO_ORES.get(item);
    }

    /**
     * 在玩家周围搜索最近的目标矿石
     *
     * @param player   执行占卜的玩家
     * @param targets  目标矿石方块列表
     * @param radius   搜索半径
     * @return 最近的矿石坐标，未找到返回 null
     */
    public static BlockPos findNearestOre(Player player, List<Block> targets, int radius) {
        BlockPos center = player.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    // 球形裁剪，减少无效搜索
                    if (dx * dx + dy * dy + dz * dz > radius * radius) continue;

                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    var state = player.level().getBlockState(cursor);

                    if (targets.contains(state.getBlock())) {
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
     * 生成粒子轨迹 — 从玩家眼部位置到目标矿石中心的金色粒子链
     *
     * @param level  服务端世界
     * @param player 占卜玩家
     * @param target 目标矿石坐标
     */
    public static void spawnGuidanceTrail(ServerLevel level, Player player, BlockPos target) {
        Vec3 start = player.getEyePosition();
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

        // 目标矿石处额外生成一圈标记粒子
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI * 2 / 8;
            double ox = Math.cos(angle) * 0.4;
            double oz = Math.sin(angle) * 0.4;
            level.sendParticles(particle,
                    end.x + ox, end.y + 0.3, end.z + oz,
                    1, 0, 0, 0, 0.02);
            level.sendParticles(particle,
                    end.x + ox, end.y + 0.8, end.z + oz,
                    1, 0, 0, 0, 0.02);
        }
    }
}
