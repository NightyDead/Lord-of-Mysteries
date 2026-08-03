package com.nightydead.lordofmysteries.datagen;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.block.AlchemyCauldronBlock;
import com.nightydead.lordofmysteries.block.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
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

        // 为各种神秘学草药生成十字交叉模型 (Cross Model)，使用 cutout 渲染层实现透明效果
        makeFlower(ModBlocks.NIGHT_PERFUME_HERB.get());
        makeFlower(ModBlocks.GOLD_MINT_HERB.get());
        makeFlower(ModBlocks.POISON_HEMLOCK_HERB.get());
        makeFlower(ModBlocks.DRAGON_BLOOD_HERB.get());
        makeFlower(ModBlocks.MANDRAKE_HERB.get());

        // 黑边太阳花：双格植物（原版向日葵同款结构），下半格茎叶、上半格带黑边的花头；模型手写于 src/main/resources
        var sunflowerBottomModel = models().getExistingFile(modLoc("block/black_edged_sunflower_bottom"));
        var sunflowerTopModel = models().getExistingFile(modLoc("block/black_edged_sunflower_top"));
        getVariantBuilder(ModBlocks.BLACK_EDGED_SUNFLOWER.get())
                .partialState().with(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER).modelForState().modelFile(sunflowerBottomModel).addModel()
                .partialState().with(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER).modelForState().modelFile(sunflowerTopModel).addModel();

        makeFlower(ModBlocks.GOLDEN_CLOAK_GRASS.get());

        // 炼药锅：生成 4 种状态的方块模型（空/有物品/成功/失败）
        registerCauldronStates();

        // 仪式祭坛：多层阶梯式自定义模型（手动编写于 src/main/resources，引用原版纹理）
        var ritualAltarModel = models().getExistingFile(modLoc("block/ritual_altar"));
        simpleBlock(ModBlocks.RITUAL_ALTAR.get(), ritualAltarModel);
        simpleBlockItem(ModBlocks.RITUAL_ALTAR.get(), ritualAltarModel);

        // 黄水晶簇：十字交叉模型（复用紫水晶簇布局），6 朝向旋转，cutout 透明渲染
        var citrineModel = models().withExistingParent("block/citrine_cluster", mcLoc("block/cross"))
                .texture("cross", modLoc("block/citrine_cluster"))
                .renderType("cutout");
        getVariantBuilder(ModBlocks.CITRINE_CLUSTER.get())
                .partialState().with(AmethystClusterBlock.FACING, Direction.DOWN).modelForState().modelFile(citrineModel).rotationX(180).addModel()
                .partialState().with(AmethystClusterBlock.FACING, Direction.EAST).modelForState().modelFile(citrineModel).rotationX(90).rotationY(90).addModel()
                .partialState().with(AmethystClusterBlock.FACING, Direction.NORTH).modelForState().modelFile(citrineModel).rotationX(90).addModel()
                .partialState().with(AmethystClusterBlock.FACING, Direction.SOUTH).modelForState().modelFile(citrineModel).rotationX(90).rotationY(180).addModel()
                .partialState().with(AmethystClusterBlock.FACING, Direction.UP).modelForState().modelFile(citrineModel).addModel()
                .partialState().with(AmethystClusterBlock.FACING, Direction.WEST).modelForState().modelFile(citrineModel).rotationX(90).rotationY(270).addModel();
        simpleBlockItem(ModBlocks.CITRINE_CLUSTER.get(), citrineModel);
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

    /**
     * 注册炼药锅的 4 种方块状态模型
     * 使用 brew_state 属性区分：0=空, 1=有物品, 2=成功, 3=失败
     * 所有状态均继承原版炼药锅模型（minecraft:block/cauldron），仅替换内部液体纹理
     */
    private void registerCauldronStates() {
        Block cauldron = ModBlocks.ALCHEMY_CAULDRON.get();

        // 为 4 种状态分别创建模型，继承原版炼药锅外形，覆盖内部液体纹理
        var emptyModel = models().withExistingParent("alchemy_cauldron_empty", mcLoc("block/cauldron"))
                .texture("content", modLoc("block/alchemy_cauldron/cauldron_inside"))
                .texture("inside", modLoc("block/alchemy_cauldron/cauldron_inside"));
        var itemsModel = models().withExistingParent("alchemy_cauldron_contains_items", mcLoc("block/cauldron"))
                .texture("content", modLoc("block/alchemy_cauldron/cauldron_inside"))
                .texture("inside", modLoc("block/alchemy_cauldron/cauldron_inside"));
        var successModel = models().withExistingParent("alchemy_cauldron_success", mcLoc("block/cauldron"))
                .texture("content", modLoc("block/alchemy_cauldron/cauldron_liquid_success"))
                .texture("inside", modLoc("block/alchemy_cauldron/cauldron_inside"));
        var failedModel = models().withExistingParent("alchemy_cauldron_failed", mcLoc("block/cauldron"))
                .texture("content", modLoc("block/alchemy_cauldron/cauldron_liquid_failed"))
                .texture("inside", modLoc("block/alchemy_cauldron/cauldron_inside"));

        // 使用 getVariantBuilder 构建多状态方块
        getVariantBuilder(cauldron)
                .partialState().with(AlchemyCauldronBlock.BREW_STATE, 0).setModels(new ConfiguredModel(emptyModel))
                .partialState().with(AlchemyCauldronBlock.BREW_STATE, 1).setModels(new ConfiguredModel(itemsModel))
                .partialState().with(AlchemyCauldronBlock.BREW_STATE, 2).setModels(new ConfiguredModel(successModel))
                .partialState().with(AlchemyCauldronBlock.BREW_STATE, 3).setModels(new ConfiguredModel(failedModel));

        // 炼药锅物品模型：指向空状态模型（继承原版炼药锅外形）
        simpleBlockItem(cauldron, emptyModel);
    }
}
