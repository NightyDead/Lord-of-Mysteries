package com.nightydead.lordofmysteries.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * 仪式祭坛方块
 * 晋升仪式核心装置，支持手持物品右键放置到祭坛顶部展示，
 * 空手 Shift+右键逆序取出最近放入的物品
 */
public class RitualAltarBlock extends Block implements EntityBlock {

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
                .strength(5.0F, 1200.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // ==================== 手持物品右键 → 放置到祭坛 ====================

    /**
     * 手持物品右键祭坛 → 将手中整组物品放置到祭坛顶部展示
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

        // 将整组物品复制到祭坛
        altar.addItem(stack.copy());

        // 消耗手中物品（创造模式除外）
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
