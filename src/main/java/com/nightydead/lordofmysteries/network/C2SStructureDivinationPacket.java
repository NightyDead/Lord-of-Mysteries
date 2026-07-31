package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.data.PlayerData;
import com.nightydead.lordofmysteries.skills.StructureDivinationHandler;
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
 * 客户端 → 服务端方向的结构占卜请求数据包
 * <p>
 * 玩家在结构占卜 UI 中选中目标结构后发送此包，
 * 服务端在玩家周围搜索对应结构并生成粒子轨迹指引方向
 */
public record C2SStructureDivinationPacket(ResourceLocation structureKey) implements CustomPacketPayload {

    public static final Type<C2SStructureDivinationPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "c2s_structure_divination"));

    private static final int SPIRITUALITY_COST = 20;
    private static final int SEARCH_RADIUS = 6000;
    private static final int SANITY_LOSS = 5;

    public static final StreamCodec<FriendlyByteBuf, C2SStructureDivinationPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC,
                    C2SStructureDivinationPacket::structureKey,
                    C2SStructureDivinationPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(TYPE, STREAM_CODEC, C2SStructureDivinationPacket::handle);
    }

    public static void handle(final C2SStructureDivinationPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            player.getExistingData(ModAttachments.PLAYER_DATA.get()).ifPresent(data -> {
                if (data.getCurrentSequence() >= 10 || !"fool".equals(data.getCurrentPathway())) {
                    player.displayClientMessage(
                            Component.translatable("message.lordofmysteries.divination.not_seer", "结构"), true);
                    return;
                }

                // 灵性检查（含堆叠消耗倍率）
                int effectiveCost = Math.round(SPIRITUALITY_COST * data.getStackCostMultiplier());
                if (data.getSpirituality() < effectiveCost) {
                    player.displayClientMessage(
                            Component.translatable("message.lordofmysteries.divination.no_spirituality", effectiveCost), true);
                    return;
                }

                data.addSpirituality(-effectiveCost);
                player.setData(ModAttachments.PLAYER_DATA.get(), data);
                PacketDistributor.sendToPlayer(player,
                        new SyncSpiritualityPacket(data.getSpirituality(), data.getMaxSpiritual()));

                // 搜索最近结构（搜索半径含堆叠能力倍率）
                int effectiveRadius = Math.round(SEARCH_RADIUS * data.getStackPowerMultiplier());
                BlockPos nearest = StructureDivinationHandler.findNearestStructure(
                        player, packet.structureKey(), effectiveRadius);

                if (nearest == null) {
                    data.addSanity(-SANITY_LOSS);
                    player.setData(ModAttachments.PLAYER_DATA.get(), data);
                    PacketDistributor.sendToPlayer(player, new SyncSanityPacket(data.getSanity()));
                    player.displayClientMessage(
                            Component.translatable("message.lordofmysteries.divination.no_response", effectiveRadius, "结构"), true);
                    return;
                }

                int gain = PlayerData.getDigestionGain(data.getCurrentSequence());
                data.addDigestion(gain);
                player.setData(ModAttachments.PLAYER_DATA.get(), data);
                PacketDistributor.sendToPlayer(player, new SyncDigestionPacket(data.getDigestion(), data.getEffectiveMaxDigestion()));

                // 固定起点，避免玩家移动导致后续波次轨迹偏移
                Vec3 startPos = player.getEyePosition();
                StructureDivinationHandler.spawnGuidanceTrail(player.serverLevel(), player, nearest);
                int baseTick = player.getServer().getTickCount();
                for (int wave = 1; wave <= 7; wave++) {
                    player.getServer().tell(new TickTask(baseTick + wave * 5,
                            () -> StructureDivinationHandler.spawnGuidanceTrail(
                                    player.serverLevel(), startPos, nearest)));
                }

                int dist = (int) Math.sqrt(player.blockPosition().distSqr(nearest));
                String structureName = Component.translatable(
                        "structure." + packet.structureKey().getNamespace() + "." + packet.structureKey().getPath()
                ).getString();
                Component successMsg = Component.translatable("message.lordofmysteries.divination.success",
                        dist, structureName);
                player.displayClientMessage(successMsg, true);
                player.getServer().tell(new TickTask(baseTick + 40,
                        () -> player.displayClientMessage(successMsg, true)));
            });
        });
    }
}
