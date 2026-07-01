package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.client.ClientDataCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 同步玩家当前途径与序列等级的网络数据包
 */
public record SyncPathwayPacket(String pathway, int sequence) implements CustomPacketPayload {

    public static final Type<SyncPathwayPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lordofmysteries", "sync_pathway"));

    // 编解码器：按顺序写入/读取 一个字符串和一个整数
    public static final StreamCodec<FriendlyByteBuf, SyncPathwayPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeUtf(packet.pathway);
                buf.writeInt(packet.sequence);
            },
            buf -> new SyncPathwayPacket(buf.readUtf(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 客户端接收处理器
     */
    public static void handle(SyncPathwayPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 将最新的途径和序列存入客户端大本营
            ClientDataCache.setPathway(payload.pathway());
            ClientDataCache.setSequence(payload.sequence());
        });
    }

    /**
     * 现代化内聚注册接口
     */
    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(TYPE, STREAM_CODEC, SyncPathwayPacket::handle);
    }
}