package com.nightydead.lordofmysteries.datagen;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.block.ModBlocks;
import com.nightydead.lordofmysteries.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * 英文语言文件数据提供者
 * 负责生成 en_us.json 语言文件，定义所有模组内容的英文翻译
 * 包括物品名称、方块名称、创造模式标签页名称等
 */
public class ModEnUsLangProvider extends LanguageProvider {

    /**
     * 构造英文语言文件提供者
     *
     * @param output 数据包输出目录
     */
    public ModEnUsLangProvider(PackOutput output) {
        super(output, LordofMysteries.MODID, "en_us");
    }

    /**
     * 注册所有英文翻译条目
     * 按类别分组：非凡特性、魔药主材、方块、创造模式标签页
     */
    @Override
    protected void addTranslations() {
        // 非凡特性物品
        add(ModItems.AGGREGATED_CHARACTERISTIC.get(), "Aggregated Characteristic");
        add(ModItems.SEER_CHARACTERISTIC.get(), "Seer Characteristic");
        add(ModItems.CLOWN_CHARACTERISTIC.get(), "Clown Characteristic");
        add(ModItems.MAGICIAN_CHARACTERISTIC.get(), "Magician Characteristic");
        add(ModItems.FACELESS_CHARACTERISTIC.get(), "Faceless Characteristic");
        add(ModItems.MARIONETTIST_CHARACTERISTIC.get(), "Marionettist Characteristic");
        add(ModItems.BIZARRO_SORCERER_CHARACTERISTIC.get(), "Bizarro Sorcerer Characteristic");
        add(ModItems.SCHOLAR_OF_YORE_CHARACTERISTIC.get(), "Scholar of Yore Characteristic");
        add(ModItems.MIRACLE_INVOKER_CHARACTERISTIC.get(), "Miracle Invoker Characteristic");
        add(ModItems.ATTENDANT_OF_MYSTERIES_CHARACTERISTIC.get(), "Attendant of Mysteries Characteristic");

        // 魔药主材与特殊物品
        add(ModItems.LAVA_OCTOPUS_BLOOD.get(), "Lava Octopus Blood");
        add(ModItems.STAR_CRYSTAL.get(), "Star Crystal");
        add(ModItems.PURE_WATER.get(), "Pure Water");
        add(ModItems.SEER_POTION.get(), "Seer Potion");

        // 魔药辅助材料
        add(ModItems.NIGHT_PERFUME_JUICE.get(), "Night Perfume Juice");
        add(ModItems.GOLD_MINT_LEAF.get(), "Gold Mint Leaf");
        add(ModItems.POISON_HEMLOCK_JUICE.get(), "Poison Hemlock Juice");
        add(ModItems.DRAGON_BLOOD_POWDER.get(), "Dragon Blood Powder");

        // 方块
        add(ModBlocks.EXAMPLE_BLOCK.get(), "Example Block");
        // 神秘学草药方块
        add(ModBlocks.NIGHT_PERFUME_HERB.get(), "Night Perfume Herb");
        add(ModBlocks.GOLD_MINT_HERB.get(), "Gold Mint Herb");
        add(ModBlocks.POISON_HEMLOCK_HERB.get(), "Poison Hemlock");
        add(ModBlocks.DRAGON_BLOOD_HERB.get(), "Dragon Blood Herb");

        // 创造模式标签页
        add("itemGroup.characteristic_tab", "Characteristic");
        add("itemGroup.potion_tab", "Potion");
        add("itemGroup.mod_block_tab", "Block");
        add("itemGroup.potion_material_tab", "Potion Material");
        add("itemGroup.potion_auxiliary_material_tab", "Potion Auxiliary Material");
    }
}
