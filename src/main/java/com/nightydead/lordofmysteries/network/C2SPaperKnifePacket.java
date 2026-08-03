package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.entity.ModEntities;
import com.nightydead.lordofmysteries.entity.PaperKnifeEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 客户端 → 服务端方向的「化纸为刀」请求数据包
 * <p>
 * 玩家在技能轮盘中选中化纸为刀后发送此包，服务端依次校验：
 * 途径/序列资格 → 灵性（含堆叠消耗倍率）→ 背包中是否有纸，
 * 全部通过后扣除 5 点灵性与 1 张纸，并以直线无重力轨迹发射纸刀
 */
public record C2SPaperKnifePacket() implements CustomPacketPayload {
    /** 数据包唯一标识 ID，注册名为 "lordofmysteries:c2s_paper_knife" */
    public static final Type<C2SPaperKnifePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "c2s_paper_knife"));

    /** 灵性消耗 */
    private static final int SPIRITUALITY_COST = 10;
    /** 纸刀飞行速度（格/tick），1.5 × 20 tick = 30 格攻击范围 */
    private static final float KNIFE_SPEED = 1.5F;

    /** 无字段数据包，使用 unit 编解码器 */
    public static final StreamCodec<FriendlyByteBuf, C2SPaperKnifePacket> STREAM_CODEC =
            StreamCodec.unit(new C2SPaperKnifePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 注册数据包到网络通道（客户端 → 服务端方向）
     *
     * @param registrar 网络注册器
     */
    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(TYPE, STREAM_CODEC, C2SPaperKnifePacket::handle);
    }

    /**
     * 服务端接收处理：资格校验 → 灵性校验 → 背包纸张校验 → 消耗 → 发射纸刀
     *
     * @param packet  接收到的数据包
     * @param context 网络上下文
     */
    public static void handle(final C2SPaperKnifePacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            // 校验：必须是愚者途径且序列 ≤ 8（小丑及以上）
            player.getExistingData(ModAttachments.PLAYER_DATA.get()).ifPresent(data -> {
                if (data.getCurrentSequence() >= 10 || !"fool".equals(data.getCurrentPathway())
                        || data.getCurrentSequence() > 8) {
                    player.displayClientMessage(
                            Component.translatable("message.lordofmysteries.paper_knife.not_clown"), true);
                    return;
                }

                // ==================== 灵性预扣检查（含堆叠消耗倍率） ====================
                int effectiveCost = Math.round(SPIRITUALITY_COST * data.getStackCostMultiplier());
                if (data.getSpirituality() < effectiveCost) {
                    player.displayClientMessage(
                            Component.translatable("message.lordofmysteries.paper_knife.no_spirituality", effectiveCost), true);
                    return;
                }

                // ==================== 背包中必须有一张纸 ====================
                int paperSlot = findPaperSlot(player);
                if (paperSlot < 0) {
                    player.displayClientMessage(
                            Component.translatable("message.lordofmysteries.paper_knife.no_paper"), true);
                    return;
                }

                // ==================== 消耗：5 点灵性 + 1 张纸 ====================
                data.addSpirituality(-effectiveCost);
                player.setData(ModAttachments.PLAYER_DATA.get(), data);
                PacketDistributor.sendToPlayer(player,
                        new SyncSpiritualityPacket(data.getSpirituality(), data.getMaxSpiritual()));
                player.getInventory().getItem(paperSlot).shrink(1);

                // ==================== 发射纸刀（直线、无重力） ====================
                PaperKnifeEntity knife = new PaperKnifeEntity(ModEntities.PAPER_KNIFE.get(), player, player.level());
                knife.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, KNIFE_SPEED, 0.0F);
                player.level().addFreshEntity(knife);
            });
        });
    }

    /**
     * 在玩家背包中查找纸的位置（含主手/副手），找不到返回 -1
     */
    private static int findPaperSlot(ServerPlayer player) {
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(Items.PAPER)) {
                return i;
            }
        }
        return -1;
    }
}
