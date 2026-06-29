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
 * ModBlock类 - 用于注册和管理模组中的方块及其对应的物品
 * 该类使用DeferredRegister系统来注册方块和物品，确保它们能够正确地被加载到游戏中
 */
public class ModBlocks {
    // 创建一个用于注册方块的DeferredRegister实例
    // 使用LordofMysteries.MODID作为注册命名空间
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(LordofMysteries.MODID);



    // 定义一个示例方块
    // 使用registerBlock方法注册，名称为"example_block"
    //方块属性设置为硬度1.5，抗性6.0
    public static final DeferredBlock<Block> EXAMPLE_BLOCK =
            registerBlock("example_block", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F)));

    /**
     * 注册方块物品的方法
     * 这是一个泛型方法，用于将方块注册为可拾取的物品
     * @param name 方块物品的注册名称
     * @param block 要注册的方块，使用DeferredBlock包装
     * @param <T> 方块的类型，必须继承自Block类
     */
    private static <T extends Block> void registerBlockItems(String name, DeferredBlock<T> block) {
        // 使用ModItems.ITEMS注册器来注册新的方块物品
        // 注册名称由参数name提供
        // 使用lambda表达式创建新的BlockItem实例
        // BlockItem使用block.get()获取的方块实例和新创建的Item.Properties来初始化
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    /**
     * 注册一个方块及其对应的物品
     * 这是一个泛型方法，用于注册任何继承自Block的方块类型
     *
     * @param <T> 方块的类型，必须是Block的子类
     * @param name 方块的注册名，通常是小写且用下划线分隔的字符串
     * @param block 方块的提供者，用于创建方块实例
     * @return 返回一个DeferredBlock对象，用于后续的方块引用
     */
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
    // 注册方块到游戏注册表，并创建一个DeferredBlock对象
        DeferredBlock<T> blocks = BLOCKS.register(name, block);
    // 同时注册该方块对应的物品
        registerBlockItems(name, blocks);
    // 返回创建的DeferredBlock对象
        return blocks;
    }

    /**
     * 注册方块事件到事件总线
     * @param eventBus 事件总线接口，用于注册方块相关事件
     */
    public static void register(IEventBus eventBus) {
    // 将方块注册到事件总线中
        BLOCKS.register(eventBus);
    }
}
