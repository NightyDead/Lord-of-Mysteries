package com.nightydead.lordofmysteries.network;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.skills.DivinationHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;

/**
 * 客户端 → 服务端方向的占卜请求数据包
 * <p>
 * 玩家在技能轮盘中选中占卜后发送此包，服务端根据玩家手持矿物
 * 搜索半径 32 格内的对应矿石，并生成粒子轨迹指引方向
 */
public record C2SDivinationPacket() implements CustomPacketPayload {
    /** 数据包唯一标识 ID，注册名为 "lordofmysteries:c2s_divination" */
    public static final Type<C2SDivinationPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "c2s_divination"));

    /** 占卜灵性消耗 */
    private static final int SPIRITUALITY_COST = 20;
    /** 占卜成功增加的消化进度（1%） */
    private static final float DIGESTION_GAIN = 0.01F;
    /** 占卜失败扣除的理智值 */
    private static final int SANITY_LOSS = 5;

    /** 无字段数据包，使用 unit 编解码器 */
    public static final StreamCodec<FriendlyByteBuf, C2SDivinationPacket> STREAM_CODEC =
            StreamCodec.unit(new C2SDivinationPacket());

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
        registrar.playToServer(TYPE, STREAM_CODEC, C2SDivinationPacket::handle);
    }

    /**
     * 服务端接收处理：校验途径资格 → 灵性预扣 → 识别矿物 → 搜索矿石 → 成败结算 → 粒子轨迹
     * <p>
     * <b>消耗与收益：</b>
     * <ul>
     *   <li>灵性不足 → 拒绝施放</li>
     *   <li>成功找到矿石 → 扣灵性 + 增加消化度 + 粒子指引</li>
     *   <li>失败（无矿物/无矿脉） → 扣灵性 + 扣少量理智</li>
     * </ul>
     *
     * @param packet  接收到的数据包
     * @param context 网络上下文
     */
    public static void handle(final C2SDivinationPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            // 校验：必须是占卜家途径的非凡者
            player.getExistingData(ModAttachments.PLAYER_DATA.get()).ifPresent(data -> {
                if (data.getCurrentSequence() >= 10 || !"fool".equals(data.getCurrentPathway())) {
                    player.displayClientMessage(
                            Component.literal("§c唯有占卜家途径的非凡者才能施展矿物占卜。"), true);
                    return;
                }

                // ==================== 灵性预扣检查 ====================
                if (data.getSpirituality() < SPIRITUALITY_COST) {
                    player.displayClientMessage(
                            Component.literal("§c灵性不足，无法施展占卜（需要 " + SPIRITUALITY_COST + " 点灵性）。"), true);
                    return;
                }

                // ==================== 扣除灵性（无论成败均消耗） ====================
                data.addSpirituality(-SPIRITUALITY_COST);
                player.setData(ModAttachments.PLAYER_DATA.get(), data);
                PacketDistributor.sendToPlayer(player,
                        new SyncSpiritualityPacket(data.getSpirituality(), data.getMaxSpiritual()));

                // 识别手持矿物（主手优先，副手兜底）
                ItemStack held = player.getMainHandItem();
                if (held.isEmpty()) {
                    held = player.getOffhandItem();
                }

                List<Block> targets = DivinationHandler.getOreTargets(held.getItem());
                if (targets == null) {
                    // 失败：扣理智
                    data.addSanity(-SANITY_LOSS);
                    player.setData(ModAttachments.PLAYER_DATA.get(), data);
                    PacketDistributor.sendToPlayer(player, new SyncSanityPacket(data.getSanity()));
                    player.displayClientMessage(
                            Component.literal("§c占卜失败"), true);
                    return;
                }

                // 搜索最近矿石
                var nearest = DivinationHandler.findNearestOre(player, targets,
                        DivinationHandler.SEARCH_RADIUS);

                if (nearest == null) {
                    // 失败：扣理智
                    data.addSanity(-SANITY_LOSS);
                    player.setData(ModAttachments.PLAYER_DATA.get(), data);
                    PacketDistributor.sendToPlayer(player, new SyncSanityPacket(data.getSanity()));
                    player.displayClientMessage(
                            Component.literal("§7灵摆毫无反应… " + DivinationHandler.SEARCH_RADIUS
                                    + " 格内未发现对应的矿石。"), true);
                    return;
                }

                // ==================== 成功：增加消化度 ====================
                data.addDigestion(DIGESTION_GAIN);
                player.setData(ModAttachments.PLAYER_DATA.get(), data);
                PacketDistributor.sendToPlayer(player, new SyncDigestionPacket(data.getDigestion()));

                // 生成粒子指引轨迹（分5波生成，每5tick一波，持续约1秒）
                DivinationHandler.spawnGuidanceTrail(player.serverLevel(), player, nearest);
                int baseTick = player.getServer().getTickCount();
                for (int wave = 1; wave <= 4; wave++) {
                    player.getServer().tell(new TickTask(baseTick + wave * 5,
                            () -> DivinationHandler.spawnGuidanceTrail(player.serverLevel(), player, nearest)));
                }

                int dist = (int) Math.sqrt(player.blockPosition().distSqr(nearest));
                player.displayClientMessage(
                        Component.literal("§6【占卜启示】§7 灵摆指向 "
                                + dist + " 格外的" + held.getDisplayName().getString() + "矿脉…"), true);
            });
        });
    }
}
