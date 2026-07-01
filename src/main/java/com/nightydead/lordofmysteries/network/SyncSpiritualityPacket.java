package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.client.ClientDataCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 同步灵性与灵性上限的网络数据包
 */
public record SyncSpiritualityPacket(int spirituality, int maxSpirituality) implements CustomPacketPayload {

    public static final Type<SyncSpiritualityPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lordofmysteries", "sync_spirituality"));

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

    public static void handle(SyncSpiritualityPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 存入客户端小仓库
            ClientDataCache.setSpirituality(payload.spirituality());
            ClientDataCache.setMaxSpirituality(payload.maxSpirituality());
        });
    }
    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(TYPE, STREAM_CODEC, SyncSpiritualityPacket::handle);
    }
}