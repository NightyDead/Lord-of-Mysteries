package com.nightydead.lordofmysteries.block;

import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.data.PlayerData;
import com.nightydead.lordofmysteries.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import com.nightydead.lordofmysteries.entity.IndestructibleItemEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 仪式祭坛方块
 * 晋升仪式核心装置：
 * - 手持物品右键 → 放置在祭坛顶部展示
 * - 手持神秘粉尘右键 → 开启分离仪式（需露天、聚合非凡特性、灵性≥80、理智≥10）
 * - 空手 Shift+右键 → 逆序取出最近放入的物品
 * 硬度等同黑曜石，需钻石镐及以上破坏才能掉落
 */
public class RitualAltarBlock extends Block implements EntityBlock {

    /** 仪式消耗灵性值 */
    private static final int RITUAL_SPIRITUALITY_COST = 80;
    /** 仪式消耗理智值 */
    private static final int RITUAL_SANITY_COST = 10;

    /** 匹配沙漏型模型的五段碰撞箱：底(16×16×3) → 收束(10×10×3) → 束腰(6×6×4) → 扩展(10×10×3) → 顶(16×16×3) */
    private static final VoxelShape SHAPE = Shapes.or(
            Shapes.box(0, 0, 0, 1, 3.0 / 16, 1),
            Shapes.box(3.0 / 16, 3.0 / 16, 3.0 / 16, 13.0 / 16, 6.0 / 16, 13.0 / 16),
            Shapes.box(5.0 / 16, 6.0 / 16, 5.0 / 16, 11.0 / 16, 10.0 / 16, 11.0 / 16),
            Shapes.box(3.0 / 16, 10.0 / 16, 3.0 / 16, 13.0 / 16, 13.0 / 16, 13.0 / 16),
            Shapes.box(0, 13.0 / 16, 0, 1, 1, 1)
    );

    public RitualAltarBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(50.0F, 1200.0F)   // 黑曜石级硬度，需要钻石镐
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // ==================== 手持物品右键 ====================

    /**
     * 手持物品右键祭坛
     * - 神秘粉尘 → 触发分离仪式
     * - 其他物品 → 放置到祭坛顶部展示
     * 创造模式下不消耗手中物品
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                               BlockPos pos, Player player, InteractionHand hand,
                                               BlockHitResult hitResult) {
        if (stack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide()) return ItemInteractionResult.sidedSuccess(true);

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RitualAltarBlockEntity altar)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 神秘粉尘 → 触发分离仪式
        if (stack.is(ModItems.MYSTIC_DUST.get())) {
            return handleRitual(stack, level, pos, player, altar);
        }

        // 其他物品 → 放置到祭坛上
        altar.addItem(stack.copy());
        if (!player.isCreative()) {
            stack.setCount(0);
        }

        return ItemInteractionResult.sidedSuccess(false);
    }

    // ==================== 空手交互 ====================

    /**
     * 空手 Shift+右键祭坛 → 逆序取出最近放入的物品
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RitualAltarBlockEntity altar)) {
            return InteractionResult.PASS;
        }

        if (altar.isEmpty()) return InteractionResult.PASS;

        ItemStack removed = altar.removeLastItem();
        if (!removed.isEmpty()) {
            if (!player.getInventory().add(removed)) {
                player.drop(removed, false);
            }
        }

        return InteractionResult.CONSUME;
    }

    // ==================== 分离仪式 ====================

    /**
     * 处理神秘粉尘右键触发的分离仪式
     * 流程：露天检测 → 寻找聚合非凡特性 → 灵性/理智检查 → 消耗资源 →
     *       分离最新混合的特性/主材 → 召唤闪电 → 更新/移除聚合特性
     */
    private ItemInteractionResult handleRitual(ItemStack dustStack, Level level, BlockPos pos,
                                                Player player, RitualAltarBlockEntity altar) {
        // 1. 露天检测
        if (!level.canSeeSky(pos.above())) {
            player.displayClientMessage(
                    Component.translatable("message.lordofmysteries.ritual.need_open_sky"), true);
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }

        // 2. 寻找祭坛上的聚合非凡特性
        int aggIndex = altar.findAggregatedCharacteristicIndex();
        if (aggIndex == -1) {
            player.displayClientMessage(
                    Component.translatable("message.lordofmysteries.ritual.need_aggregated"), true);
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }

        ItemStack aggStack = altar.getItem(aggIndex);
        // 防御性拷贝：get() 返回值在NBT反序列化/网络同步后可能不可变
        List<String> features = aggStack.get(ModDataComponents.AGGREGATED_FEATURES.get());
        if (features == null || features.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.lordofmysteries.ritual.no_features"), true);
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        features = new ArrayList<>(features); // 确保可变拷贝

        // 3. 获取玩家数据，检查灵性与理智
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        if (data.getSpirituality() < RITUAL_SPIRITUALITY_COST) {
            player.displayClientMessage(
                    Component.translatable("message.lordofmysteries.ritual.need_spirituality",
                            RITUAL_SPIRITUALITY_COST), true);
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (data.getSanity() < RITUAL_SANITY_COST) {
            player.displayClientMessage(
                    Component.translatable("message.lordofmysteries.ritual.need_sanity",
                            RITUAL_SANITY_COST), true);
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }

        // 4. 分离最新混合的特性/主材（LIFO），先执行分离确保格式正确再消耗资源
        String lastFeature = features.remove(features.size() - 1);
        ItemStack separatedItem = createSeparatedItem(lastFeature, aggStack);
        if (separatedItem.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.lordofmysteries.ritual.separate_failed"), true);
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }

        // 5. 消耗灵性、理智与神秘粉尘
        data.addSpirituality(-RITUAL_SPIRITUALITY_COST);
        data.addSanity(-RITUAL_SANITY_COST);
        if (!player.isCreative()) {
            dustStack.shrink(1);
        }

        // 6. 召唤闪电劈中祭坛
        LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
        lightning.moveTo(Vec3.atBottomCenterOf(pos.above()));
        level.addFreshEntity(lightning);

        // 7. 将分离出的物品掉落在祭坛上方（不灭实体，免疫闪电火焰与物理伤害）
        IndestructibleItemEntity itemEntity = new IndestructibleItemEntity(
                level, pos.getX() + 0.5, pos.getY() + 1.5,
                pos.getZ() + 0.5, separatedItem);
        itemEntity.setDeltaMovement(0, 0.15, 0); // 给一点向上初速度，视觉更明显
        itemEntity.setPickUpDelay(40); // 2秒延迟，让玩家看清闪电后掉落的产物
        level.addFreshEntity(itemEntity);

        // 8. 更新或移除聚合非凡特性
        if (features.isEmpty()) {
            altar.removeItemAt(aggIndex);
        } else {
            aggStack.set(ModDataComponents.AGGREGATED_FEATURES.get(), features);
            altar.setItem(aggIndex, aggStack);
        }

        player.displayClientMessage(
                Component.translatable("message.lordofmysteries.ritual.success"), true);

        return ItemInteractionResult.sidedSuccess(false);
    }

    /**
     * 根据聚合特性中存储的特征字符串创建对应的分离物品
     * - 失败酿造标记 → 特征字符串为显示名称 → 查找魔药主材物品
     * - 正常聚合特性 → 特征字符串为 "pathway:sequence" → 查找非凡特性物品
     *
     * @param feature  特征字符串
     * @param aggStack 聚合特性物品堆叠（用于读取 FAILED_BREW_MARKER）
     * @return 分离出的物品堆叠，无法解析时返回空堆叠
     */
    private ItemStack createSeparatedItem(String feature, ItemStack aggStack) {
        Boolean isFailedBrew = aggStack.get(ModDataComponents.FAILED_BREW_MARKER.get());

        if (Boolean.TRUE.equals(isFailedBrew)) {
            // 失败酿造：特征字符串是魔药主材的显示名称
            var item = ModItems.findMainMaterialByDisplayName(feature);
            if (item != null) {
                return new ItemStack(item);
            }
        } else {
            // 正常聚合：特征字符串格式为 "pathway:sequence"
            String[] parts = feature.split(":");
            if (parts.length == 2) {
                try {
                    String pathway = parts[0];
                    int sequence = Integer.parseInt(parts[1]);
                    var characteristicItem = ModItems.getPureCharacteristic(pathway, sequence);
                    if (characteristicItem != null) {
                        return new ItemStack(characteristicItem);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return ItemStack.EMPTY;
    }

    // ==================== 方块破坏处理 ====================

    /**
     * 方块被移除时，将祭坛上所有物品掉落到世界中
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos,
                            BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RitualAltarBlockEntity altar) {
                for (ItemStack stack : altar.getItems()) {
                    if (!stack.isEmpty()) {
                        ItemEntity itemEntity = new ItemEntity(
                                level, pos.getX() + 0.5, pos.getY() + 1.0,
                                pos.getZ() + 0.5, stack.copy());
                        level.addFreshEntity(itemEntity);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // ==================== EntityBlock 接口 ====================

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RitualAltarBlockEntity(pos, state);
    }
}
