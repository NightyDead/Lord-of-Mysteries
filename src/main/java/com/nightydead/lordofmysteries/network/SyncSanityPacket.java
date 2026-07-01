package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.client.ClientDataCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 同步理智的网络数据包
 * 负责将服务端的当前理智值高效率地同步至客户端
 */
public record SyncSanityPacket(int sanity) implements CustomPacketPayload {

    // 数据包的唯一识别 ID
    public static final Type<SyncSanityPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lordofmysteries", "sync_sanity"));

    // 流式编解码器（用于将数据写入网络缓冲区，或者从缓冲区读取）
    public static final StreamCodec<FriendlyByteBuf, SyncSanityPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeInt(packet.sanity), // 写入
            buf -> new SyncSanityPacket(buf.readInt())    // 读取
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 🔥 核心：客户端收到这个数据包时的处理逻辑
     */
    public static void handle(SyncSanityPacket payload, IPayloadContext context) {
        // 确保在主线程（渲染/客户端线程）执行，避免多线程并发问题
        context.enqueueWork(() -> {
            // 🌟 将收到的理智值存入客户端本地的缓存中
            ClientDataCache.setSanity(payload.sanity());
        });
    }
    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(TYPE, STREAM_CODEC, SyncSanityPacket::handle);
    }
}