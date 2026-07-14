package com.nightydead.lordofmysteries.block;

import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.data.PlayerData;
import com.nightydead.lordofmysteries.item.custom.RitualDaggerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * 炼药锅方块
 * 无UI交互的魔药炼制装置，支持右键放入/取出材料、灵性注入触发酿造
 * 类似机械动力工作盆的交互模式
 */
public class AlchemyCauldronBlock extends Block implements EntityBlock {

    /** 酿造状态属性：0=空, 1=有物品, 2=成功, 3=失败 */
    public static final IntegerProperty BREW_STATE = IntegerProperty.create("brew_state", 0, 3);

    /**
     * 炼药锅碰撞形状（与原版炼药锅一致）
     * 底部薄板 + 四面墙壁，内部空心
     */
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 4, 16),   // 底部
            Block.box(0, 4, 0, 2, 16, 16),    // 西墙
            Block.box(14, 4, 0, 16, 16, 16),  // 东墙
            Block.box(0, 4, 0, 16, 16, 2),    // 北墙
            Block.box(0, 4, 14, 16, 16, 16)   // 南墙
    );

    public AlchemyCauldronBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BREW_STATE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BREW_STATE);
    }

    /**
     * 获取方块的碰撞形状
     * 返回与原版炼药锅相同的空心形状，防止玩家走进锅内
     */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                   CollisionContext context) {
        return SHAPE;
    }

    /**
     * 客户端粒子和动画效果
     * 失败态（brew_state=3）时在锅口上方生成黑烟粒子
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(BREW_STATE) == 3) {
            double x = pos.getX() + 0.25 + random.nextDouble() * 0.5;
            double y = pos.getY() + 0.9;
            double z = pos.getZ() + 0.25 + random.nextDouble() * 0.5;
            level.addParticle(ParticleTypes.SMOKE, x, y, z,
                    (random.nextDouble() - 0.5) * 0.02,
                    random.nextDouble() * 0.05 + 0.02,
                    (random.nextDouble() - 0.5) * 0.02);
        }
    }

    // ==================== 物品交互（手持物品右键） ====================

    /**
     * 手持物品右键炼药锅 → 将物品放入锅内
     * 仅在锅未酿造完成时允许放入
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                               BlockPos pos, Player player, InteractionHand hand,
                                               BlockHitResult hitResult) {
        // 空手时交给 useWithoutItem 处理
        if (stack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide()) return ItemInteractionResult.sidedSuccess(level.isClientSide());

        // 仪式匕首：实际交互由 ModEventHandlers.onRightClickBlock 事件处理，这里防止误放入
        if (stack.getItem() instanceof RitualDaggerItem) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AlchemyCauldronBlockEntity cauldron)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 酿造完成后不允许放入物品
        if (cauldron.isBrewed()) {
            player.displayClientMessage(Component.translatable(
                    "message.lordofmysteries.cauldron.brewed_block"), true);
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 放入一个物品到锅内
        ItemStack toInsert = stack.copyWithCount(1);
        if (cauldron.addItem(toInsert)) {
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            // 更新方块状态为"有物品"
            level.setBlock(pos, state.setValue(BREW_STATE, 1), Block.UPDATE_ALL);
            player.displayClientMessage(Component.translatable(
                    "message.lordofmysteries.cauldron.item_added"), true);
            return ItemInteractionResult.sidedSuccess(false);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // ==================== 空手交互 ====================

    /**
     * 空手右键炼药锅
     * 行为优先级：
     * 1. Shift + 空手 + 锅内有物品 + 未酿造 → 注入灵性触发酿造
     * 2. 酿造成功 + 手持玻璃瓶 → 取出魔药
     * 3. 酿造成功 → 提示使用玻璃瓶
     * 4. 酿造失败 → 取出聚合非凡特性
     * 5. 锅内有物品（未酿造）→ 逆序弹出最后一个材料
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AlchemyCauldronBlockEntity cauldron)) {
            return InteractionResult.PASS;
        }

        // === 行为 1：Shift + 空手 → 非凡者注入灵性触发酿造 ===
        if (player.isShiftKeyDown() && !cauldron.isEmpty() && !cauldron.isBrewed()) {
            return handleBrewTrigger(level, pos, player, cauldron);
        }

        // === 行为 2/3：酿造成功 → 提示/取出魔药 ===
        if (cauldron.getBrewState() == AlchemyCauldronBlockEntity.STATE_SUCCESS) {
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.is(Items.GLASS_BOTTLE)) {
                // 消耗玻璃瓶，给予魔药
                if (!level.isClientSide()) {
                    ItemStack potion = cauldron.takeResult();
                    if (!potion.isEmpty()) {
                        if (!player.isCreative()) {
                            mainHand.shrink(1);
                        }
                        if (mainHand.isEmpty()) {
                            player.setItemInHand(InteractionHand.MAIN_HAND, potion);
                        } else if (!player.getInventory().add(potion)) {
                            player.drop(potion, false);
                        }
                        level.setBlock(pos, state.setValue(BREW_STATE, 0), Block.UPDATE_ALL);
                        player.displayClientMessage(Component.translatable(
                                "message.lordofmysteries.cauldron.potion_taken"), true);
                    }
                }
                return InteractionResult.CONSUME;
            } else {
                player.displayClientMessage(Component.translatable(
                        "message.lordofmysteries.cauldron.use_bottle"), true);
                return InteractionResult.CONSUME;
            }
        }

        // === 行为 4：酿造失败 → 取出聚合非凡特性 ===
        if (cauldron.getBrewState() == AlchemyCauldronBlockEntity.STATE_FAILED) {
            ItemStack result = cauldron.takeResult();
            if (!result.isEmpty()) {
                if (!player.getInventory().add(result)) {
                    player.drop(result, false);
                }
                level.setBlock(pos, state.setValue(BREW_STATE, 0), Block.UPDATE_ALL);
                player.displayClientMessage(Component.translatable(
                        "message.lordofmysteries.cauldron.characteristic_taken"), true);
            }
            return InteractionResult.CONSUME;
        }

        // === 行为 5：锅内有物品（未酿造）→ 逆序弹出最后一个材料 ===
        if (!cauldron.isEmpty()) {
            ItemStack removed = cauldron.removeLastItem();
            if (!removed.isEmpty()) {
                if (!player.getInventory().add(removed)) {
                    player.drop(removed, false);
                }
                if (cauldron.isEmpty()) {
                    level.setBlock(pos, state.setValue(BREW_STATE, 0), Block.UPDATE_ALL);
                }
                player.displayClientMessage(Component.translatable(
                        "message.lordofmysteries.cauldron.item_removed"), true);
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    // ==================== 酿造触发 ====================

    /**
     * 处理非凡者注入灵性触发酿造
     * 检查玩家是否为非凡者并有足够灵性，消耗 5 点灵性后触发酿造
     */
    private InteractionResult handleBrewTrigger(Level level, BlockPos pos, Player player,
                                                 AlchemyCauldronBlockEntity cauldron) {
        PlayerData data = player.getData(ModAttachments.PLAYER_DATA.get());
        String pathway = data.getCurrentPathway();
        boolean isBeyonder = !"none".equals(pathway) && data.getCurrentSequence() < 10;

        if (!isBeyonder) {
            player.displayClientMessage(Component.translatable(
                    "message.lordofmysteries.cauldron.need_beyonder"), true);
            return InteractionResult.CONSUME;
        }

        int spirituality = data.getSpirituality();
        if (spirituality < 5) {
            player.displayClientMessage(Component.translatable(
                    "message.lordofmysteries.cauldron.need_spirituality"), true);
            return InteractionResult.CONSUME;
        }

        // 消耗 5 点灵性
        data.addSpirituality(-5);
        // 触发酿造
        cauldron.triggerBrew(player);

        // 更新方块状态
        BlockState currentState = level.getBlockState(pos);
        int newState = cauldron.getBrewState();
        level.setBlock(pos, currentState.setValue(BREW_STATE, newState), Block.UPDATE_ALL);

        return InteractionResult.CONSUME;
    }

    // ==================== 方块破坏处理 ====================

    /**
     * 方块被移除时，将内部物品或酿造结果掉落到世界中
     * 未酿造 → 掉落所有放入的材料
     * 已酿造 → 掉落酿造结果（成功=魔药，失败=聚合非凡特性）
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos,
                            BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AlchemyCauldronBlockEntity cauldron) {
                for (ItemStack drop : cauldron.getDrops()) {
                    if (!drop.isEmpty()) {
                        ItemEntity itemEntity =
                                new ItemEntity(
                                        level, pos.getX() + 0.5, pos.getY() + 1.0,
                                        pos.getZ() + 0.5, drop);
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
        return new AlchemyCauldronBlockEntity(pos, state);
    }
}
