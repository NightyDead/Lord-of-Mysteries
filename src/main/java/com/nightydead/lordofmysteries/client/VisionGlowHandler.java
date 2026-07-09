package com.nightydead.lordofmysteries.client;

import com.nightydead.lordofmysteries.LordofMysteries;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 灵视实体发光处理器（客户端侧）
 * 当玩家开启灵视时，将周围一定范围内的活体生物标记为发光状态，
 * 使其在灵界幽蓝滤镜下呈现轮廓高亮效果，模拟“看穿灵界生物”的视觉体验。
 * <p>
 * 关闭灵视后，自动清除之前标记的发光效果，还原原版渲染。
 * <p>
 * 实现原理：利用 Minecraft 原生的 {@link LivingEntity#setGlowingTag(boolean)} 机制，
 * 客户端设置后会通过实体数据同步触发 Outline Shader 渲染发光轮廓。
 * <p>
 * 注意：服务端也有对应的发光处理逻辑（见 ModEventHandlers），
 * 服务端设置的发光标记会通过实体数据同步到客户端，双重保障确保发光效果可靠渲染。
 */
@EventBusSubscriber(modid = LordofMysteries.MODID, value = Dist.CLIENT)
public class
VisionGlowHandler {

    /** 灵视感知半径（方块单位） */
    private static final double VISION_RANGE = 32.0D;

    /** 检测频率：每 5 tick（0.25秒）扫描一次周围实体，减少性能开销 */
    private static final int SCAN_INTERVAL = 5;

    /** 记录由灵视标记为发光的实体 ID 集合，用于关闭灵视时精确还原 */
    private static final Set<Integer> visionGlowingEntities = new HashSet<>();

    /** 上一帧灵视是否激活，用于检测状态变化 */
    private static boolean wasVisionActive = false;

    /**
     * 客户端 Tick 事件处理
     * 根据灵视状态动态管理周围生物的发光标记
     *
     * @param event 客户端 Tick 事件
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        boolean visionActive = ClientDataCache.isSpiritVisionActive();

        // 检测灵视状态变化，输出调试日志
        if (visionActive != wasVisionActive) {
            LordofMysteries.LOGGER.info("[VisionGlow] 灵视状态切换: {} -> {}, 当前追踪实体数: {}",
                    wasVisionActive, visionActive, visionGlowingEntities.size());
            wasVisionActive = visionActive;
        }

        // 灵视关闭时：清除所有由灵视标记的发光实体
        if (!visionActive) {
            if (!visionGlowingEntities.isEmpty()) {
                LordofMysteries.LOGGER.info("[VisionGlow] 清除灵视发光实体 {} 个", visionGlowingEntities.size());
                clearAllGlowing(mc);
            }
            return;
        }

        // 灵视开启时：每 SCAN_INTERVAL tick 扫描一次周围实体
        if (mc.player.tickCount % SCAN_INTERVAL != 0) return;

        // 清理已卸载/死亡的实体记录
        cleanupInvalidEntities(mc);

        // 扫描并标记周围活体生物为发光状态
        var nearbyEntities = mc.level.getEntitiesOfClass(
                LivingEntity.class,
                mc.player.getBoundingBox().inflate(VISION_RANGE),
                entity -> entity != mc.player && entity.isAlive()
        );

        int marked = 0;
        for (LivingEntity entity : nearbyEntities) {
            if (!entity.isCurrentlyGlowing()) {
                entity.setGlowingTag(true);
                visionGlowingEntities.add(entity.getId());
                marked++;
            }
        }

        if (marked > 0) {
            LordofMysteries.LOGGER.info("[VisionGlow] 新标记 {} 个实体发光, 总追踪数: {}", marked, visionGlowingEntities.size());
        }
    }

    /**
     * 清理已失效的实体记录（实体已卸载或死亡）
     *
     * @param mc Minecraft 客户端实例
     */
    private static void cleanupInvalidEntities(Minecraft mc) {
        if (mc.level == null) return;

        Iterator<Integer> iterator = visionGlowingEntities.iterator();
        while (iterator.hasNext()) {
            int entityId = iterator.next();
            var entity = mc.level.getEntity(entityId);
            if (entity == null || !entity.isAlive() || !(entity instanceof LivingEntity)) {
                iterator.remove();
            }
        }
    }

    /**
     * 清除所有由灵视标记的发光实体
     * 在灵视关闭或玩家离开世界时调用
     */
    private static void clearAllGlowing(Minecraft mc) {
        if (mc.level == null) {
            visionGlowingEntities.clear();
            return;
        }

        for (int entityId : visionGlowingEntities) {
            var entity = mc.level.getEntity(entityId);
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.setGlowingTag(false);
            }
        }
        visionGlowingEntities.clear();
    }
}
