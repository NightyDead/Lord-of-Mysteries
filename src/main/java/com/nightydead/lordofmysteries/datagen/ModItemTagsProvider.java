package com.nightydead.lordofmysteries.datagen;

import com.nightydead.lordofmysteries.LordofMysteries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * 物品标签数据提供者
 * 负责为模组物品生成物品标签 JSON 文件
 * 当前为空实现，后续可在此添加物品标签分组（如 forge:ores、minecraft:planks 等）
 */
public class ModItemTagsProvider extends ItemTagsProvider {

    /**
     * 构造物品标签提供者
     *
     * @param output             数据包输出目录
     * @param lookupProvider     注册表查找器的异步Future
     * @param blockTags          方块标签的查找结果，用于自动继承方块标签到物品标签
     * @param existingFileHelper 已有文件检查器
     */
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, LordofMysteries.MODID, existingFileHelper);
    }

    /**
     * 注册所有物品标签
     * 当前为空实现，后续可在此添加物品标签定义
     *
     * @param provider 注册表查找器
     */
    @Override
    protected void addTags(HolderLookup.Provider provider) {

    }
}
