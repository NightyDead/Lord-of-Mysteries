package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.ModAttachments;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端 -> 服务端方向的灵视切换数据包
 * 玩家按下灵视快捷键时发送此包，服务端接收后切换玩家的灵视状态
 * <p>
 * 灵视开启条件：玩家必须已踏上超凡道路（序列号 < 10）
 * 灵视状态会同步到客户端供 HUD 渲染使用
 */
public record C2SToggleVisionPacket() implements CustomPacketPayload {
    /** 数据包的唯一标识 ID，注册名为 "lordofmysteries:c2s_toggle_vision" */
    public static final Type<C2SToggleVisionPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "c2s_toggle_vision"));

    /** 流式编解码器：无字段数据包，使用 unit 编解码器 */
    public static final StreamCodec<FriendlyByteBuf, C2SToggleVisionPacket> STREAM_CODEC = StreamCodec.unit(new C2SToggleVisionPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    /**
     * 注册数据包到网络通道（客户端 → 服务端方向）
     *
     * @param registrar 网络注册器
     */
    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(TYPE, STREAM_CODEC, C2SToggleVisionPacket::handle);
    }

    /**
     * 服务端接收处理：切换玩家的灵视状态
     * 通过 enqueueWork 确保在服务端主线程执行，避免多线程并发问题
     *
     * @param packet  接收到的数据包
     * @param context 网络上下文
     */
    public static void handle(final C2SToggleVisionPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                player.getExistingData(ModAttachments.PLAYER_DATA.get()).ifPresent(data -> {
                    if (data.getCurrentSequence() >= 10) {
                        player.sendSystemMessage(Component.literal("§c凡俗之躯无法触碰灵界，你未能开启灵视。"));
                        return;
                    }

                    boolean currentStatus = data.isVisionActive();
                    boolean newStatus = !currentStatus;

                    // 🔮 开启灵视时检查灵性：灵性为 0 无法开启
                    if (newStatus && data.getSpirituality() <= 0 && !player.isCreative()) {
                        player.sendSystemMessage(Component.literal("§c灵性枯竭，无法开启灵视。"));
                        return;
                    }

                    data.setVisionActive(newStatus);
                    player.setData(ModAttachments.PLAYER_DATA.get(), data);

                    // 🔑 关键修复：将灵视状态同步回客户端，更新 ClientDataCache 以驱动 HUD 渲染和实体发光
                    PacketDistributor.sendToPlayer(player, new SyncVisionPacket(newStatus));

                    if (newStatus) {
                        player.sendSystemMessage(Component.literal("§5【神秘学启示】§7 灵光在你眼底蔓延，你开启了灵视..."));
                    } else {
                        player.sendSystemMessage(Component.literal("§7 灵光隐去，你退出了灵视。"));
                    }

                });
            }
        });
    }
}