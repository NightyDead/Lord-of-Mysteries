package com.nightydead.lordofmysteries.datagen;

import com.nightydead.lordofmysteries.block.ModBlocks;
import com.nightydead.lordofmysteries.item.ModItems;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicates;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
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

        // 黑边太阳花：双格植物，仅下半格掉落自身（与原版向日葵一致）
        this.add(ModBlocks.BLACK_EDGED_SUNFLOWER.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModBlocks.BLACK_EDGED_SUNFLOWER.get())
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.BLACK_EDGED_SUNFLOWER.get())
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))))
                        .when(ExplosionCondition.survivesExplosion())));

        dropSelf(ModBlocks.GOLDEN_CLOAK_GRASS.get());

        // 炼药锅掉落自身（内部物品由 BlockEntity.onRemove 处理）
        dropSelf(ModBlocks.ALCHEMY_CAULDRON.get());

        // 仪式祭坛掉落自身
        dropSelf(ModBlocks.RITUAL_ALTAR.get());

        // 黄水晶簇：与紫水晶簇一致的掉落逻辑 ——
        // 精准采集掉落簇自身；用 #cluster_max_harvestables 工具挖掘掉落 4 个黄水晶碎片（时运加成）；其他工具掉落 2 个碎片（爆炸衰减）
        var silkTouch = this.registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        var fortune = this.registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
        this.add(ModBlocks.CITRINE_CLUSTER.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(AlternativesEntry.alternatives(
                                // 分支 1：精准采集 → 掉落黄水晶簇自身
                                LootItem.lootTableItem(ModBlocks.CITRINE_CLUSTER.get())
                                        .when(MatchTool.toolMatches(ItemPredicate.Builder.item()
                                                .withSubPredicate(ItemSubPredicates.ENCHANTMENTS,
                                                        ItemEnchantmentsPredicate.enchantments(List.of(
                                                                new EnchantmentPredicate(silkTouch, MinMaxBounds.Ints.atLeast(1))))))),
                                // 分支 2：非精准采集（嵌套 alternatives）
                                AlternativesEntry.alternatives(
                                        // 分支 2a：使用 #cluster_max_harvestables 工具（与原版紫水晶簇相同）→ 掉落 4 个碎片 + 时运加成
                                        LootItem.lootTableItem(ModItems.CITRINE_SHARD.get())
                                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4.0F)))
                                                .apply(ApplyBonusCount.addOreBonusCount(fortune))
                                                .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.CLUSTER_MAX_HARVESTABLES))),
                                        // 分支 2b：其他工具 → 掉落 2 个碎片 + 爆炸衰减
                                        LootItem.lootTableItem(ModItems.CITRINE_SHARD.get())
                                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)))
                                                .apply(ApplyExplosionDecay.explosionDecay()))))));
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
