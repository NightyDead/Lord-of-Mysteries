package com.nightydead.lordofmysteries.block;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.item.ModItems;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
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
            registerBlock("example_block", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F)
                    .requiresCorrectToolForDrops()));

    /** 夜香草 - 吃掉或神秘学互动时可赋予幸运效果，持续5秒/100刻 */
    public static final DeferredBlock<Block> NIGHT_PERFUME_HERB =
            registerBlock("night_perfume_herb",
                    () -> new HerbBlock(MobEffects.NIGHT_VISION, 5.0F, BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));

    /** 金薄荷 - 魔药辅助材料来源植物 */
    public static final DeferredBlock<Block> GOLD_MINT_HERB =
            registerBlock("gold_mint_herb",
                    () -> new HerbBlock(MobEffects.LUCK, 5.0F, BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));

    /** 毒堇 - 带有一点毒性效果的魔药辅助材料来源植物 */
    public static final DeferredBlock<Block> POISON_HEMLOCK_HERB =
            registerBlock("poison_hemlock_herb",
                    () -> new HerbBlock(MobEffects.POISON, 5.0F, BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));

    /** 龙血草 - 魔药辅助材料来源植物 */
    public static final DeferredBlock<Block> DRAGON_BLOOD_HERB =
            registerBlock("dragon_blood_herb",
                    () -> new HerbBlock(MobEffects.FIRE_RESISTANCE, 5.0F, BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));

    /** 曼陀罗 - 剧毒植物，小丑魔药辅材来源 */
    public static final DeferredBlock<Block> MANDRAKE_HERB =
            registerBlock("mandrake_herb",
                    () -> new HerbBlock(MobEffects.POISON, 5.0F, BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));

    /** 黑边太阳花 - 小丑魔药辅材来源，双格植物（茎叶+带黑边的花头） */
    public static final DeferredBlock<BlackEdgedSunflowerBlock> BLACK_EDGED_SUNFLOWER =
            registerBlock("black_edged_sunflower",
                    () -> new BlackEdgedSunflowerBlock(MobEffects.GLOWING, 5.0F, BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));

    /** 金斗篷草 - 小丑魔药辅材来源 */
    public static final DeferredBlock<Block> GOLDEN_CLOAK_GRASS =
            registerBlock("golden_cloak_grass",
                    () -> new HerbBlock(MobEffects.ABSORPTION, 5.0F, BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));

    /** 炼药锅 - 无UI交互的魔药炼制装置，支持右键放入/取出材料、灵性注入触发酿造 */
    public static final DeferredBlock<AlchemyCauldronBlock> ALCHEMY_CAULDRON =
            registerBlock("alchemy_cauldron",
                    () -> new AlchemyCauldronBlock(BlockBehaviour.Properties.of()
                            .strength(3.5F, 6.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)
                            .mapColor(MapColor.METAL)
                            .noOcclusion()));

    /** 仪式祭坛 - 晋升仪式核心装置，支持右键放置物品展示，Shift+右键取出 */
    public static final DeferredBlock<RitualAltarBlock> RITUAL_ALTAR =
            registerBlock("ritual_altar", RitualAltarBlock::new);

    /** 黄水晶簇 - 洞穴中自然生成的水晶矿物，复用原版紫水晶簇的方块行为（发光、可附着生长） */
    public static final DeferredBlock<AmethystClusterBlock> CITRINE_CLUSTER =
            registerBlock("citrine_cluster", () -> new AmethystClusterBlock(7, 3, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .forceSolidOn()
                    .noOcclusion()
                    .randomTicks()
                    .strength(1.5F)
                    .sound(SoundType.AMETHYST_CLUSTER)
                    .lightLevel(state -> 5)));

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
