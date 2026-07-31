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

/**
 * 方块标签数据提供者
 * 负责为模组方块生成方块标签 JSON 文件
 * 标签用于定义方块的分组属性（如可挖掘工具类型、植物分类等）
 */
public class ModBlockTagsProvider extends BlockTagsProvider {

    /**
     * 构造方块标签提供者
     *
     * @param packOutput         数据包输出目录
     * @param lookupProvider     注册表查找器的异步Future
     * @param existingFileHelper 已有文件检查器
     */
    public ModBlockTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, lookupProvider, LordofMysteries.MODID, existingFileHelper);
    }

    /**
     * 注册所有方块标签
     * 将模组方块分配到对应的原版标签中
     *
     * @param provider 注册表查找器
     */
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 示例方块标记为镐子可挖掘
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.EXAMPLE_BLOCK.get())
                .add(ModBlocks.RITUAL_ALTAR.get());

        // 仪式祭坛需要钻石镐及以上才能破坏掉落（等同黑曜石）
        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocks.RITUAL_ALTAR.get());

        // 添加各种神秘学草药到小花标签，允许它们种植在泥土、草方块上
        tag(BlockTags.SMALL_FLOWERS)
                .add(ModBlocks.NIGHT_PERFUME_HERB.get())
                .add(ModBlocks.GOLD_MINT_HERB.get())
                .add(ModBlocks.POISON_HEMLOCK_HERB.get())
                .add(ModBlocks.DRAGON_BLOOD_HERB.get())
                .add(ModBlocks.MANDRAKE_HERB.get())
                .add(ModBlocks.BLACK_EDGED_SUNFLOWER.get())
                .add(ModBlocks.GOLDEN_CLOAK_GRASS.get());
    }
}
