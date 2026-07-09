package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.client.ClientDataCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 服务端 -> 客户端方向的灵视状态同步数据包
 * 当玩家的灵视状态在服务端发生变化时，发送此包通知客户端更新
 * 客户端接收后将状态存入 {@link ClientDataCache} 供 HUD 渲染使用
 *
 * @param isActive 当前灵视是否开启
 */
public record SyncVisionPacket(boolean isActive) implements CustomPacketPayload {
    /** 数据包的唯一标识 ID，注册名为 "lordofmysteries:sync_vision" */
    public static final Type<SyncVisionPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "sync_vision"));

    /** 流式编解码器：写入/读取一个 boolean 值作为灵视状态 */
    public static final StreamCodec<FriendlyByteBuf, SyncVisionPacket> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> buf.writeBoolean(pkt.isActive),
            buf -> new SyncVisionPacket(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    /**
     * 注册数据包到网络通道（服务端 → 客户端方向）
     *
     * @param registrar 网络注册器
     */
    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(TYPE, STREAM_CODEC, SyncVisionPacket::handle);
    }

    /**
     * 客户端接收处理：将灵视状态存入 {@link ClientDataCache}
     * 通过 enqueueWork 确保在客户端主线程执行，避免多线程并发问题
     *
     * @param packet  接收到的数据包
     * @param context 网络上下文
     */
    public static void handle(final SyncVisionPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientDataCache.setSpiritVisionActive(packet.isActive());
        });
    }
}