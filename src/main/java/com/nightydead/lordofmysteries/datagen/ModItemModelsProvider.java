package com.nightydead.lordofmysteries.datagen;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelsProvider extends ItemModelProvider {
    public ModItemModelsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, LordofMysteries.MODID, existingFileHelper);
    }

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
        // basicItem(ModItems.STAR_CRYSTAL.get());                   // 缺少 textures/item/material/star_crystal.png
        basicItem(ModItems.PURE_WATER.get());
        basicItem(ModItems.SEER_POTION.get());

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
