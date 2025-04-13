package com.nightydead.lordofmysteries.item;

import com.nightydead.lordofmysteries.LordOfMysteries;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item ICEICE = registerItems("iceice", new Item(new Item.Settings()));

    private static Item registerItems(String id, Item item){
//        return Registry.register(Registries.ITEM, RegistryKey.of(Registries.ITEM.getKey(), Identifier.of(LordOfMysteries.MOD_ID, id)), item);
        return Registry.register(Registries.ITEM, Identifier.of(LordOfMysteries.MOD_ID, id), item);
    }

    public static void registerModItems(){
        LordOfMysteries.LOGGER.info("Registering Items");
    }
}
