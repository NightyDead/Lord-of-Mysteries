package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.client.ClientDataCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.HashSet;
import java.util.Set;

/**
 * 同步已学魔药配方到客户端
 * 服务端 → 客户端方向，登录时和每次学习后触发
 *
 * @param recipes 已学配方集合，格式 "pathway:sequence"
 */
public record SyncLearnedRecipesPacket(Set<String> recipes) implements CustomPacketPayload {

    /** 数据包唯一标识 ID */
    public static final Type<SyncLearnedRecipesPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("lordofmysteries", "sync_learned_recipes"));

    /** 流式编解码器：先写条目数，再逐个写字符串 */
    public static final StreamCodec<FriendlyByteBuf, SyncLearnedRecipesPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.recipes.size());
                for (String r : packet.recipes) {
                    buf.writeUtf(r);
                }
            },
            buf -> {
                int size = buf.readVarInt();
                Set<String> set = new HashSet<>();
                for (int i = 0; i < size; i++) {
                    set.add(buf.readUtf());
                }
                return new SyncLearnedRecipesPacket(set);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 客户端接收处理：将已学配方集合存入 {@link ClientDataCache}
     */
    public static void handle(SyncLearnedRecipesPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientDataCache.setLearnedRecipes(payload.recipes());
        });
    }

    /**
     * 注册数据包到网络通道（服务端 → 客户端方向）
     */
    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(TYPE, STREAM_CODEC, SyncLearnedRecipesPacket::handle);
    }
}
