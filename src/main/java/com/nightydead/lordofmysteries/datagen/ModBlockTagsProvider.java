package com.nightydead.lordofmysteries.datagen;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;


import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, lookupProvider, LordofMysteries.MODID, existingFileHelper);
    }
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
        .add(ModBlocks.EXAMPLE_BLOCK.get());

        // 添加植物到小花标签，允许它们种植在泥土、草方块上
        tag(BlockTags.SMALL_FLOWERS)
                .add(ModBlocks.NIGHT_PERFUME_HERB.get())
                .add(ModBlocks.GOLD_MINT_HERB.get())
                .add(ModBlocks.POISON_HEMLOCK_HERB.get())
                .add(ModBlocks.DRAGON_BLOOD_HERB.get());
    }
}
