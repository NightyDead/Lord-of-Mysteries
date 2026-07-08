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

/**
 * 模组数据生成器入口类
 * 监听 GatherDataEvent 事件，在运行数据生成器时统一注册所有 Provider
 * 包括战利品表、配方、标签、物品模型、方块状态、语言文件等数据提供者
 * <p>
 * 使用 @EventBusSubscriber 注解自动注册到模组事件总线
 */
@EventBusSubscriber(modid = LordofMysteries.MODID)
public class ModDataGenerator {

    /**
     * 数据生成事件回调 - 注册所有数据提供者
     * 根据 event.includeServer() 判断是否为服务端数据生成模式
     *
     * @param event 数据收集事件，提供 DataGenerator、PackOutput、ExistingFileHelper 等工具
     */
    @SubscribeEvent
    public static void generateData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // 注册方块战利品表提供者
        gen.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTablesProvider::new, LootContextParamSets.BLOCK)), lookupProvider));
        // 注册配方提供者
        gen.addProvider(event.includeServer(), new ModRecipesProvider(packOutput, lookupProvider));

        // 注册方块标签提供者（需要先于物品标签提供者创建）
        BlockTagsProvider blockTagsProvider = new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper);
        gen.addProvider(event.includeServer(), blockTagsProvider);
        // 注册物品标签提供者（依赖方块标签的查找结果）
        gen.addProvider(event.includeServer(), new ModItemTagsProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));

        // 注册物品模型提供者
        gen.addProvider(event.includeServer(), new ModItemModelsProvider(packOutput, existingFileHelper));
        // 注册方块状态与模型提供者
        gen.addProvider(event.includeServer(), new ModBlockStatesProvider(packOutput, existingFileHelper));
        // 注册英文语言文件提供者
        gen.addProvider(event.includeServer(), new ModEnUsLangProvider(packOutput));
        // 注册中文语言文件提供者
        gen.addProvider(event.includeServer(), new ModZhCnLangProvider(packOutput));
    }
}
