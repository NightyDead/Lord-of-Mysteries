package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.client.ClientDataCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 同步扮演法消化进度的网络数据包（0.0F ~ 1.0F）
 */
public record SyncDigestionPacket(float digestion) implements CustomPacketPayload {

    public static final Type<SyncDigestionPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("lordofmysteries", "sync_digestion"));

    public static final StreamCodec<FriendlyByteBuf, SyncDigestionPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeFloat(packet.digestion),
            buf -> new SyncDigestionPacket(buf.readFloat())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncDigestionPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientDataCache.setDigestion(payload.digestion());
        });
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(TYPE, STREAM_CODEC, SyncDigestionPacket::handle);
    }
}