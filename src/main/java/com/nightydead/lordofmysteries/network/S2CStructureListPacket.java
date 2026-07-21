package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.LordofMysteries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务端 → 客户端：响应客户端请求的结构列表
 * <p>
 * 结构注册表仅在服务端存在，客户端无法直接访问。
 * 当玩家打开结构占卜 UI 时，客户端发送 {@link C2SRequestStructuresPacket}，
 * 服务端从此注册表读取所有结构后通过此包发回客户端。
 */
public record S2CStructureListPacket(List<StructureInfo> structures) implements CustomPacketPayload {

    public static final Type<S2CStructureListPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "s2c_structure_list"));

    public static final StreamCodec<FriendlyByteBuf, S2CStructureListPacket> STREAM_CODEC =
            StreamCodec.of(S2CStructureListPacket::encode, S2CStructureListPacket::decode);

    private static void encode(FriendlyByteBuf buf, S2CStructureListPacket packet) {
        buf.writeVarInt(packet.structures.size());
        for (StructureInfo info : packet.structures) {
            buf.writeResourceLocation(info.key());
            buf.writeUtf(info.displayName());
            buf.writeVarInt(info.tags().size());
            for (String tag : info.tags()) {
                buf.writeUtf(tag);
            }
        }
    }

    private static S2CStructureListPacket decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<StructureInfo> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation key = buf.readResourceLocation();
            String displayName = buf.readUtf();
            int tagCount = buf.readVarInt();
            List<String> tags = new ArrayList<>(tagCount);
            for (int j = 0; j < tagCount; j++) {
                tags.add(buf.readUtf());
            }
            list.add(new StructureInfo(key, displayName, tags));
        }
        return new S2CStructureListPacket(list);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(TYPE, STREAM_CODEC, S2CStructureListPacket::handle);
    }

    public static void handle(final S2CStructureListPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            // 将服务端发来的结构列表写入 StructureDivinationScreen 的静态接收缓冲区
            com.nightydead.lordofmysteries.client.gui.StructureDivinationScreen.receiveStructureList(packet.structures());
        });
    }

    /**
     * 单个结构条目信息
     */
    public record StructureInfo(ResourceLocation key, String displayName, List<String> tags) {}
}
