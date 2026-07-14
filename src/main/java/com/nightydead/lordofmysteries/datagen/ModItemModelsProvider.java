package com.nightydead.lordofmysteries.datagen;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * 物品模型数据提供者
 * 负责为模组中所有物品生成物品模型 JSON 文件
 * 使用 generated 模型（2D扁平图标）作为默认物品模型类型
 */
public class ModItemModelsProvider extends ItemModelProvider {

    /**
     * 构造物品模型提供者
     *
     * @param output             数据包输出目录
     * @param existingFileHelper 已有文件检查器，用于验证纹理资源是否存在
     */
    public ModItemModelsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, LordofMysteries.MODID, existingFileHelper);
    }

    /**
     * 注册所有物品的模型
     * 注释掉的物品表示纹理文件尚未准备就绪，待添加纹理后取消注释即可
     */
    @Override
    protected void registerModels() {
        // TODO: 为以下物品添加纹理后取消注释以生成模型
        // basicItem(ModItems.AGGREGATED_CHARACTERISTIC.get());      // 缺少 textures/item/characteristic/aggregated_characteristic.png
        basicItem(ModItems.SEER_CHARACTERISTIC.get());
        // basicItem(ModItems.CLOWN_CHARACTERISTIC.get());           // 缺少 textures/item/characteristic/clown_characteristic.png
        // basicItem(ModItems.MAGICIAN_CHARACTERISTIC.get());        // 缺少 textures/item/characteristic/magician_characteristic.png
        // basicItem(ModItems.FACELESS_CHARACTERISTIC.get());        // 缺少 textures/item/characteristic/faceless_characteristic.png
        // basicItem(ModItems.MARIONETTIST_CHARACTERISTIC.get());    // 缺少 textures/item/characteristic/marionettist_characteristic.png
        // basicItem(ModItems.BIZARRO_SORCERER_CHARACTERISTIC.get()); // 缺少 textures/item/characteristic/bizarro_sorcerer_characteristic.png
        // basicItem(ModItems.SCHOLAR_OF_YORE_CHARACTERISTIC.get()); // 缺少 textures/item/characteristic/scholar_of_yore_characteristic.png
        // basicItem(ModItems.MIRACLE_INVOKER_CHARACTERISTIC.get()); // 缺少 textures/item/characteristic/miracle_invoker_characteristic.png
        // basicItem(ModItems.ATTENDANT_OF_MYSTERIES_CHARACTERISTIC.get()); // 缺少 textures/item/characteristic/attendant_of_mysteries_characteristic.png
        basicItem(ModItems.LAVA_OCTOPUS_BLOOD.get());
        basicItem(ModItems.STAR_CRYSTAL.get());
        basicItem(ModItems.PURE_WATER.get());
        basicItem(ModItems.SEER_POTION.get());
        basicItem(ModItems.RITUAL_DAGGER.get());

        // 为植物的 BlockItem 生成 2D 物品模型，指向 textures/block/ 目录下的贴图
        makeBlockItemModel("night_perfume_herb");
        makeBlockItemModel("gold_mint_herb");
        makeBlockItemModel("poison_hemlock_herb");
        makeBlockItemModel("dragon_blood_herb");
    }

    // 辅助方法：生成 BlockItem 使用的扁平生成的物品模型
    private void makeBlockItemModel(String name) {
        withExistingParent(name, mcLoc("item/generated"))
                .texture("layer0", modLoc("block/" + name));
    }
}
