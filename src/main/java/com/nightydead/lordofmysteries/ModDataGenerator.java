package com.nightydead.lordofmysteries;

import com.nightydead.lordofmysteries.datagen.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = LordofMysteries.MODID)
public class ModDataGenerator {
    @SubscribeEvent
    public static void generateData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        gen.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTablesProvider::new, LootContextParamSets.BLOCK)), lookupProvider));
        gen.addProvider(event.includeServer(), new ModRecipesProvider(packOutput, lookupProvider));

        BlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper);
        gen.addProvider(event.includeServer(), blockTagsProvider);
        gen.addProvider(event.includeServer(), new ModItemTagsProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));

        gen.addProvider(event.includeServer(), new ModItemModelsProvider(packOutput, existingFileHelper));
        gen.addProvider(event.includeServer(), new ModBlockStatesProvider(packOutput, existingFileHelper));
        gen.addProvider(event.includeServer(), new ModEnUsLangProvider(packOutput));
        gen.addProvider(event.includeServer(), new ModZhCnLangProvider(packOutput));
    }
}
