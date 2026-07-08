package com.nightydead.lordofmysteries.datagen;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.block.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * 方块状态与模型数据提供者
 * 负责为模组中所有方块生成 blockstate JSON 和对应的模型 JSON 文件
 * 继承 NeoForge 的 BlockStateProvider 以使用便捷的模型构建工具
 */
public class ModBlockStatesProvider extends BlockStateProvider {

    /**
     * 构造方块状态提供者
     *
     * @param packOutput       数据包输出目录
     * @param existingFileHelper 已有文件检查器，用于验证纹理等资源是否存在
     */
    public ModBlockStatesProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, LordofMysteries.MODID, existingFileHelper);
    }

    /**
     * 注册所有方块的状态与模型
     * 包括普通立方体方块和花朵类十字交叉模型方块
     */
    @Override
    protected void registerStatesAndModels() {
        // 示例方块：生成立方体模型及对应的物品模型
        simpleBlockWithItem(ModBlocks.EXAMPLE_BLOCK.get(), cubeAll(ModBlocks.EXAMPLE_BLOCK.get()));

        // 为四种神秘学草药生成十字交叉模型 (Cross Model)，使用 cutout 渲染层实现透明效果
        makeFlower(ModBlocks.NIGHT_PERFUME_HERB.get());
        makeFlower(ModBlocks.GOLD_MINT_HERB.get());
        makeFlower(ModBlocks.POISON_HEMLOCK_HERB.get());
        makeFlower(ModBlocks.DRAGON_BLOOD_HERB.get());
    }

    /**
     * 辅助方法：为花朵类方块快捷生成十字交叉 blockstate 和模型
     * 使用 cutout 渲染类型实现透明背景效果
     *
     * @param block 要生成模型的花朵方块
     */
    private void makeFlower(Block block) {
        simpleBlock(block, models().cross(blockTexture(block).getPath(), blockTexture(block)).renderType("cutout"));
    }
}
