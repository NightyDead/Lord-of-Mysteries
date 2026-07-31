package com.nightydead.lordofmysteries.datagen;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Map;
import java.util.function.Supplier;

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
        // 通用纹理路径（尚未为每个物品制作独立纹理，统一使用通用纹理）
        ResourceLocation charTexture = modLoc("item/characteristic/aggregated_characteristic");
        ResourceLocation potionTexture = modLoc("item/potion/seer_potion");

        // 批量生成全途径非凡特性物品模型（必须加 item/ 前缀，否则模型会生成到错误的 models/ 目录下）
        for (Map.Entry<String, Map<Integer, Supplier<Item>>> pwEntry : ModItems.CHARACTERISTIC_MAP.entrySet()) {
            for (Map.Entry<Integer, Supplier<Item>> seqEntry : pwEntry.getValue().entrySet()) {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(seqEntry.getValue().get());
                withExistingParent("item/" + key.getPath(), mcLoc("item/generated"))
                        .texture("layer0", charTexture);
            }
        }
        // 批量生成全途径魔药物品模型
        for (Map.Entry<String, Map<Integer, Supplier<Item>>> pwEntry : ModItems.POTION_MAP.entrySet()) {
            for (Map.Entry<Integer, Supplier<Item>> seqEntry : pwEntry.getValue().entrySet()) {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(seqEntry.getValue().get());
                withExistingParent("item/" + key.getPath(), mcLoc("item/generated"))
                        .texture("layer0", potionTexture);
            }
        }

        // 聚合非凡特性（独立注册，不在 CHARACTERISTIC_MAP 中，需单独生成模型）
        withExistingParent("item/characteristic/aggregated_characteristic", mcLoc("item/generated"))
                .texture("layer0", charTexture);

        basicItem(ModItems.LAVA_OCTOPUS_BLOOD.get());
        basicItem(ModItems.STAR_CRYSTAL.get());
        basicItem(ModItems.PURE_WATER.get());
        basicItem(ModItems.RITUAL_DAGGER.get());
        basicItem(ModItems.MYSTIC_DUST.get());

        // 为植物的 BlockItem 生成 2D 物品模型，指向 textures/block/ 目录下的贴图
        makeBlockItemModel("night_perfume_herb");
        makeBlockItemModel("gold_mint_herb");
        makeBlockItemModel("poison_hemlock_herb");
        makeBlockItemModel("dragon_blood_herb");
        makeBlockItemModel("mandrake_herb");
        makeBlockItemModel("black_edged_sunflower");
        makeBlockItemModel("golden_cloak_grass");
    }

    // 辅助方法：生成 BlockItem 使用的扁平生成的物品模型
    private void makeBlockItemModel(String name) {
        withExistingParent(name, mcLoc("item/generated"))
                .texture("layer0", modLoc("block/" + name));
    }
}
