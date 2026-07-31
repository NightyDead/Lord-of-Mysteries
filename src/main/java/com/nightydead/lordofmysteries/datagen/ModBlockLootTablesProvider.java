package com.nightydead.lordofmysteries.datagen;

import com.nightydead.lordofmysteries.block.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;


/**
 * 方块战利品表数据提供者
 * 负责为模组中所有方块生成对应的战利品表 JSON 文件
 * 继承 BlockLootSubProvider 以使用 NeoForge 提供的战利品表构建工具
 */
public class ModBlockLootTablesProvider extends BlockLootSubProvider {

    /**
     * 构造战利品表提供者
     *
     * @param registries 注册表查找器，用于解析动态注册表中的引用
     */
    public ModBlockLootTablesProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    /**
     * 注册所有方块的战利品表
     * 当前所有方块均使用 dropSelf 策略（破坏后直接掉落自身）
     */
    @Override
    protected void generate() {
        // 示例方块掉落自身
        dropSelf(ModBlocks.EXAMPLE_BLOCK.get());

        // 各种神秘学草药掉落自身
        dropSelf(ModBlocks.NIGHT_PERFUME_HERB.get());
        dropSelf(ModBlocks.GOLD_MINT_HERB.get());
        dropSelf(ModBlocks.POISON_HEMLOCK_HERB.get());
        dropSelf(ModBlocks.DRAGON_BLOOD_HERB.get());
        dropSelf(ModBlocks.MANDRAKE_HERB.get());
        dropSelf(ModBlocks.BLACK_EDGED_SUNFLOWER.get());
        dropSelf(ModBlocks.GOLDEN_CLOAK_GRASS.get());

        // 炼药锅掉落自身（内部物品由 BlockEntity.onRemove 处理）
        dropSelf(ModBlocks.ALCHEMY_CAULDRON.get());

        // 仪式祭坛掉落自身
        dropSelf(ModBlocks.RITUAL_ALTAR.get());
    }

    /**
     * 返回所有已注册的方块列表，用于战利品表生成器的完整性校验
     *
     * @return 已知方块的可迭代集合
     */
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
