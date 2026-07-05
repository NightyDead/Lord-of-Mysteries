package com.nightydead.lordofmysteries.datagen;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.block.ModBlocks;
import com.nightydead.lordofmysteries.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModEnUsLangProvider extends LanguageProvider {
    public ModEnUsLangProvider(PackOutput output) {
        super(output, LordofMysteries.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
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
        add(ModItems.LAVA_OCTOPUS_BLOOD.get(), "Lava Octopus Blood");
        add(ModItems.STAR_CRYSTAL.get(), "Star Crystal");
        add(ModItems.PURE_WATER.get(), "Pure Water");
        add(ModItems.SEER_POTION.get(), "Seer Potion");
        add(ModItems.NIGHT_PERFUME_JUICE.get(), "Night Perfume Juice");
        add(ModItems.GOLD_MINT_LEAF.get(), "Gold Mint Leaf");
        add(ModItems.POISON_HEMLOCK_JUICE.get(), "Poison Hemlock Juice");
        add(ModItems.DRAGON_BLOOD_POWDER.get(), "Dragon Blood Powder");

        add(ModBlocks.EXAMPLE_BLOCK.get(), "Example Block");

        add("itemGroup.characteristic_tab", "Characteristic");
        add("itemGroup.potion_tab", "Potion");
        add("itemGroup.mod_block_tab", "Block");
        add("itemGroup.potion_material_tab", "Potion Material");
        add("itemGroup.potion_auxiliary_material_tab", "Potion Auxiliary Material");
    }
}
