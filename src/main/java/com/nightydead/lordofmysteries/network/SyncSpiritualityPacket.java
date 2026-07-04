package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.client.ClientDataCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 同步灵性值与灵性上限的网络数据包
 * 将服务端的当前灵性和灵性上限同步至客户端，供 HUD 渲染灵性进度条
 *
 * @param spirituality    当前灵性值
 * @param maxSpirituality 灵性上限值
 */
public record SyncSpiritualityPacket(int spirituality, int maxSpirituality) implements CustomPacketPayload {

    /** 数据包的唯一标识 ID，注册名为 "lordofmysteries:sync_spirituality" */
    public static final Type<SyncSpiritualityPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lordofmysteries", "sync_spirituality"));

    /** 流式编解码器：按顺序写入/读取当前灵性（int）和灵性上限（int） */
    public static final StreamCodec<FriendlyByteBuf, SyncSpiritualityPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.spirituality);
                buf.writeInt(packet.maxSpirituality);
            },
            buf -> new SyncSpiritualityPacket(buf.readInt(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 客户端接收处理：将灵性值和灵性上限存入 {@link ClientDataCache}
     *
     * @param payload 接收到的数据包
     * @param context 网络上下文
     */
    public static void handle(SyncSpiritualityPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientDataCache.setSpirituality(payload.spirituality());
            ClientDataCache.setMaxSpirituality(payload.maxSpirituality());
        });
    }

    /**
     * 注册数据包到网络通道（服务端 → 客户端方向）
     *
     * @param registrar 网络注册器
     */
    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(TYPE, STREAM_CODEC, SyncSpiritualityPacket::handle);
    }
}
