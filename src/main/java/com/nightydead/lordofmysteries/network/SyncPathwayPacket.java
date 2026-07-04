package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.client.ClientDataCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 同步玩家途径与序列等级的网络数据包
 * 将服务端玩家当前的非凡途径 ID 和序列号同步至客户端，供 HUD 渲染身份看板
 *
 * @param pathway  当前途径 ID（如 "fool"），"none" 表示凡人
 * @param sequence 当前序列号（0~9），10 表示凡人
 */
public record SyncPathwayPacket(String pathway, int sequence) implements CustomPacketPayload {

    /** 数据包的唯一标识 ID，注册名为 "lordofmysteries:sync_pathway" */
    public static final Type<SyncPathwayPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lordofmysteries", "sync_pathway"));

    /** 流式编解码器：按顺序写入/读取途径名称（String）和序列号（int） */
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
     * 客户端接收处理：将途径 ID 和序列号存入 {@link ClientDataCache}
     *
     * @param payload 接收到的数据包
     * @param context 网络上下文
     */
    public static void handle(SyncPathwayPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientDataCache.setPathway(payload.pathway());
            ClientDataCache.setSequence(payload.sequence());
        });
    }

    /**
     * 注册数据包到网络通道（服务端 → 客户端方向）
     *
     * @param registrar 网络注册器
     */
    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(TYPE, STREAM_CODEC, SyncPathwayPacket::handle);
    }
}
