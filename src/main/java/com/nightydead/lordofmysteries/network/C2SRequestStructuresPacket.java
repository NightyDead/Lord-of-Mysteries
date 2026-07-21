package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.LordofMysteries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户端 → 服务端：请求当前世界的所有结构列表
 * <p>
 * 客户端在打开结构占卜 UI 时发送此包，服务端从 {@link Registries#STRUCTURE}
 * 注册表中收集所有已注册结构的信息（名称、标签）后通过 {@link S2CStructureListPacket} 返回
 */
public record C2SRequestStructuresPacket() implements CustomPacketPayload {

    public static final Type<C2SRequestStructuresPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "c2s_request_structures"));

    public static final StreamCodec<FriendlyByteBuf, C2SRequestStructuresPacket> STREAM_CODEC =
            StreamCodec.unit(new C2SRequestStructuresPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(TYPE, STREAM_CODEC, C2SRequestStructuresPacket::handle);
    }

    public static void handle(final C2SRequestStructuresPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            var structureRegistry = player.serverLevel().registryAccess()
                    .registryOrThrow(Registries.STRUCTURE);
            List<S2CStructureListPacket.StructureInfo> list = new ArrayList<>();

            for (var entry : structureRegistry.entrySet()) {
                ResourceLocation key = entry.getKey().location();
                Holder<Structure> holder = structureRegistry.getHolderOrThrow(
                        structureRegistry.getResourceKey(entry.getValue()).orElseThrow());

                List<String> tags = holder.tags()
                        .map(TagKey::location)
                        .map(ResourceLocation::toString)
                        .collect(Collectors.toList());

                String nameTranslated = Component.translatable(
                        "structure." + key.getNamespace() + "." + key.getPath()).getString();
                // 如果翻译键未找到（返回的是键本身），格式化路径为可读名称
                String name;
                if (nameTranslated.equals("structure." + key.getNamespace() + "." + key.getPath())) {
                    name = formatStructurePath(key.getPath());
                } else {
                    name = nameTranslated;
                }

                list.add(new S2CStructureListPacket.StructureInfo(key, name, tags));
            }

            PacketDistributor.sendToPlayer(player, new S2CStructureListPacket(list));
        });
    }

    /** 将 snake_case 结构路径格式化为可读名称（如 "village_plains" → "Village Plains"） */
    private static String formatStructurePath(String path) {
        StringBuilder sb = new StringBuilder();
        boolean capitalize = true;
        for (char c : path.toCharArray()) {
            if (c == '_') {
                sb.append(' ');
                capitalize = true;
            } else if (capitalize) {
                sb.append(Character.toUpperCase(c));
                capitalize = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
