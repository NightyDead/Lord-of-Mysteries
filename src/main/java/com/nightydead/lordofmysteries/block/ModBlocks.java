package com.nightydead.lordofmysteries.block;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 模组方块注册类
 * 使用 DeferredRegister 系统注册方块及其对应的方块物品，
 * 确保方块在游戏加载时正确注册到注册表中
 */
public class ModBlocks {

    /** 方块延迟注册表，使用模组 ID 作为命名空间 */
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(LordofMysteries.MODID);

    /**
     * 示例方块
     * 硬度 1.5（挖掘时间参考石头），爆炸抗性 6.0
     */
    public static final DeferredBlock<Block> EXAMPLE_BLOCK =
            registerBlock("example_block", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F)));

    /**
     * 注册方块对应的方块物品（使方块可以被拾取和放置在物品栏中）
     *
     * @param name  方块物品的注册名
     * @param block 已注册的方块引用
     * @param <T>   方块类型，必须继承自 Block
     */
    private static <T extends Block> void registerBlockItems(String name, DeferredBlock<T> block) {
        // 在 ModItems 的物品注册表中注册一个 BlockItem，使方块可作为物品使用
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    /**
     * 注册一个方块并同时注册其对应的方块物品
     * 内部依次调用 BLOCKS.register() 和 registerBlockItems()
     *
     * @param <T>   方块类型，必须是 Block 的子类
     * @param name  方块的注册名（小写加下划线格式）
     * @param block 方块实例的提供者（Supplier）
     * @return 注册后的 DeferredBlock 引用
     */
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        // 注册方块到游戏注册表
        DeferredBlock<T> blocks = BLOCKS.register(name, block);
        // 同时注册该方块对应的物品
        registerBlockItems(name, blocks);
        return blocks;
    }

    /**
     * 将方块注册表绑定到模组事件总线
     *
     * @param eventBus 模组事件总线
     */
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
