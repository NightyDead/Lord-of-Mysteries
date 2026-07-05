package com.nightydead.lordofmysteries.datagen;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.block.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStatesProvider extends BlockStateProvider {
    public ModBlockStatesProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, LordofMysteries.MODID, existingFileHelper);
    }
    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(ModBlocks.EXAMPLE_BLOCK.get(), cubeAll(ModBlocks.EXAMPLE_BLOCK.get()));

        // 为植物生成十字交叉模型 (Cross Model) 并且使用 cutout 渲染层
        makeFlower(ModBlocks.NIGHT_PERFUME_HERB.get());
        makeFlower(ModBlocks.GOLD_MINT_HERB.get());
        makeFlower(ModBlocks.POISON_HEMLOCK_HERB.get());
        makeFlower(ModBlocks.DRAGON_BLOOD_HERB.get());
    }

    // 辅助方法：快捷生成花朵的 blockstate 和 model
    private void makeFlower(Block block) {
        simpleBlock(block, models().cross(blockTexture(block).getPath(), blockTexture(block)).renderType("cutout"));
    }
}
