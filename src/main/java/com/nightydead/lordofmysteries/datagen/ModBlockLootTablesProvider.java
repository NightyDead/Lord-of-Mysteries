package com.nightydead.lordofmysteries.datagen;

import com.nightydead.lordofmysteries.block.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;


public class ModBlockLootTablesProvider extends BlockLootSubProvider {
    public ModBlockLootTablesProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.EXAMPLE_BLOCK.get());

        dropSelf(ModBlocks.NIGHT_PERFUME_HERB.get());
        dropSelf(ModBlocks.GOLD_MINT_HERB.get());
        dropSelf(ModBlocks.POISON_HEMLOCK_HERB.get());
        dropSelf(ModBlocks.DRAGON_BLOOD_HERB.get());
    }
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
