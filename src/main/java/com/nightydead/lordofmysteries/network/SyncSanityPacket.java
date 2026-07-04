package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.client.ClientDataCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 同步理智值的网络数据包
 * 将服务端的当前理智值同步至客户端，供 HUD 渲染理智进度条
 *
 * @param sanity 当前理智值（0~100）
 */
public record SyncSanityPacket(int sanity) implements CustomPacketPayload {

    /** 数据包的唯一标识 ID，注册名为 "lordofmysteries:sync_sanity" */
    public static final Type<SyncSanityPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lordofmysteries", "sync_sanity"));

    /** 流式编解码器：写入/读取一个 int 值作为理智值 */
    public static final StreamCodec<FriendlyByteBuf, SyncSanityPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeInt(packet.sanity),
            buf -> new SyncSanityPacket(buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 客户端接收处理：将理智值存入 {@link ClientDataCache}
     * 通过 enqueueWork 确保在客户端主线程执行，避免多线程并发问题
     *
     * @param payload 接收到的数据包
     * @param context 网络上下文
     */
    public static void handle(SyncSanityPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientDataCache.setSanity(payload.sanity());
        });
    }

    /**
     * 注册数据包到网络通道（服务端 → 客户端方向）
     *
     * @param registrar 网络注册器
     */
    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(TYPE, STREAM_CODEC, SyncSanityPacket::handle);
    }
}
