package com.nightydead.lordofmysteries.item;

import com.nightydead.lordofmysteries.LordOfMysteries;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item MOYAO = registerItems("moyao", new Item(new Item.Settings()));

    private static Item registerItems(String id, Item item){
//        return Registry.register(Registries.ITEM, RegistryKey.of(Registries.ITEM.getKey(), Identifier.of(LordOfMysteries.MOD_ID, id)), item);
        return Registry.register(Registries.ITEM, Identifier.of(LordOfMysteries.MOD_ID, id), item);
    }

    private static void addItemToIG(FabricItemGroupEntries fabricItemGroupEntries){
        fabricItemGroupEntries.add(MOYAO);
    }

    public static void registerModItems(){
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(ModItems::addItemToIG);
        LordOfMysteries.LOGGER.info("Registering Items");
    }
}
