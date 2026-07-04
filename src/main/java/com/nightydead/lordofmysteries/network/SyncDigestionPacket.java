package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.client.ClientDataCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 同步魔药消化进度的网络数据包
 * 将服务端的消化进度（0.0F ~ 1.0F）同步至客户端，供 HUD 渲染消化度进度条
 *
 * @param digestion 当前消化进度，0.0F 表示未消化，1.0F 表示完全消化
 */
public record SyncDigestionPacket(float digestion) implements CustomPacketPayload {

    /** 数据包的唯一标识 ID，注册名为 "lordofmysteries:sync_digestion" */
    public static final Type<SyncDigestionPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lordofmysteries", "sync_digestion"));

    /** 流式编解码器：写入/读取一个 float 值作为消化进度 */
    public static final StreamCodec<FriendlyByteBuf, SyncDigestionPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeFloat(packet.digestion),
            buf -> new SyncDigestionPacket(buf.readFloat())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 客户端接收处理：将消化进度存入 {@link ClientDataCache}
     * 通过 enqueueWork 确保在客户端主线程执行，避免多线程并发问题
     *
     * @param payload 接收到的数据包
     * @param context 网络上下文
     */
    public static void handle(SyncDigestionPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientDataCache.setDigestion(payload.digestion());
        });
    }

    /**
     * 注册数据包到网络通道（服务端 → 客户端方向）
     *
     * @param registrar 网络注册器
     */
    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(TYPE, STREAM_CODEC, SyncDigestionPacket::handle);
    }
}
