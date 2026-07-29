package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.data.PlayerData;
import com.nightydead.lordofmysteries.skills.BiomeDivinationHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 客户端 → 服务端方向的群系占卜请求数据包
 * <p>
 * 玩家在群系占卜 UI 中选中目标群系后发送此包，
 * 服务端在玩家周围搜索对应群系并生成粒子轨迹指引方向
 */
public record C2SBiomeDivinationPacket(ResourceLocation biomeKey) implements CustomPacketPayload {

    /** 数据包唯一标识 ID */
    public static final Type<C2SBiomeDivinationPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "c2s_biome_divination"));

    /** 群系占卜灵性消耗 */
    private static final int SPIRITUALITY_COST = 20;
    /** 搜索半径（方块） */
    private static final int SEARCH_RADIUS = 6000;
    /** 搜索步长（方块），越大越快但精度越低 */
    private static final int SEARCH_STEP = 128;
    /** 占卜失败扣除的理智值 */
    private static final int SANITY_LOSS = 5;

    /** 编解码器：ResourceLocation + unit 模式 */
    public static final StreamCodec<FriendlyByteBuf, C2SBiomeDivinationPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC,
                    C2SBiomeDivinationPacket::biomeKey,
                    C2SBiomeDivinationPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 注册数据包到网络通道（客户端 → 服务端方向）
     */
    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(TYPE, STREAM_CODEC, C2SBiomeDivinationPacket::handle);
    }

    /**
     * 服务端接收处理：校验途径 → 灵性预扣 → 搜索群系 → 粒子指引
     */
    public static void handle(final C2SBiomeDivinationPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            player.getExistingData(ModAttachments.PLAYER_DATA.get()).ifPresent(data -> {
                // 校验：必须是占卜家途径
                if (data.getCurrentSequence() >= 10 || !"fool".equals(data.getCurrentPathway())) {
                    player.displayClientMessage(
                            Component.literal("§c唯有占卜家途径的非凡者才能施展群系占卜。"), true);
                    return;
                }

                // 灵性检查（含堆叠消耗倍率）
                int effectiveCost = Math.round(SPIRITUALITY_COST * data.getStackCostMultiplier());
                if (data.getSpirituality() < effectiveCost) {
                    player.displayClientMessage(
                            Component.literal("§c灵性不足，无法施展占卜（需要 " + effectiveCost + " 点灵性）。"), true);
                    return;
                }

                // 扣除灵性
                data.addSpirituality(-effectiveCost);
                player.setData(ModAttachments.PLAYER_DATA.get(), data);
                PacketDistributor.sendToPlayer(player,
                        new SyncSpiritualityPacket(data.getSpirituality(), data.getMaxSpiritual()));

                // 搜索最近的目标群系位置（搜索半径含堆叠能力倍率）
                int effectiveRadius = Math.round(SEARCH_RADIUS * data.getStackPowerMultiplier());
                BlockPos nearest = BiomeDivinationHandler.findNearestBiome(
                        player, packet.biomeKey(), effectiveRadius, SEARCH_STEP);

                if (nearest == null) {
                    // 失败：扣理智
                    data.addSanity(-SANITY_LOSS);
                    player.setData(ModAttachments.PLAYER_DATA.get(), data);
                    PacketDistributor.sendToPlayer(player, new SyncSanityPacket(data.getSanity()));
                    player.displayClientMessage(
                            Component.literal("§7灵摆毫无反应… " + effectiveRadius
                                    + " 格内未发现目标群系。"), true);
                    return;
                }

                // ==================== 成功：增加消化度 ====================
                int gain = PlayerData.getDigestionGain(data.getCurrentSequence());
                data.addDigestion(gain);
                player.setData(ModAttachments.PLAYER_DATA.get(), data);
                PacketDistributor.sendToPlayer(player, new SyncDigestionPacket(data.getDigestion(), data.getEffectiveMaxDigestion()));

                // 生成粒子指引轨迹（分8波生成，每5tick一波，持续约1.75秒）
                // 固定起点，避免玩家移动导致后续波次轨迹偏移
                Vec3 startPos = player.getEyePosition();
                BiomeDivinationHandler.spawnGuidanceTrail(player.serverLevel(), player, nearest);
                int baseTick = player.getServer().getTickCount();
                for (int wave = 1; wave <= 7; wave++) {
                    player.getServer().tell(new TickTask(baseTick + wave * 5,
                            () -> BiomeDivinationHandler.spawnGuidanceTrail(
                                    player.serverLevel(), startPos, nearest)));
                }

                int dist = (int) Math.sqrt(player.blockPosition().distSqr(nearest));
                String biomeName = Component.translatable(
                        "biome." + packet.biomeKey().getNamespace() + "." + packet.biomeKey().getPath()
                ).getString();
                Component successMsg = Component.literal("§6【占卜启示】§7 灵摆指向 "
                        + dist + " 格外的" + biomeName + "群系…");
                player.displayClientMessage(successMsg, true);
                // 40 tick 后重发一次，延长显示时间
                player.getServer().tell(new TickTask(baseTick + 40,
                        () -> player.displayClientMessage(successMsg, true)));
            });
        });
    }
}
