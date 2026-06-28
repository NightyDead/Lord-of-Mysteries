package com.nightydead.lordofmysteries.item;

import com.nightydead.lordofmysteries.LordofMysteries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(LordofMysteries.MODID);
    //非凡特性
    //region 占卜家途径
    public static final DeferredItem<Item> SEER_CHARACTERISTIC =
            ITEMS.register("seer_characteristic", () -> new Item(new Item.Properties().fireResistant()));
    public static final DeferredItem<Item> CLOWN_CHARACTERISTIC =
            ITEMS.register("clown_characteristic", () -> new Item(new Item.Properties().fireResistant()));
    public static final DeferredItem<Item> MAGICIAN_CHARACTERISTIC =
            ITEMS.register("magician_characteristic", () -> new Item(new Item.Properties().fireResistant()));
    public static final DeferredItem<Item> FACELESS_CHARACTERISTIC =
            ITEMS.register("faceless_characteristic", () -> new Item(new Item.Properties().fireResistant()));
    public static final DeferredItem<Item> MARIONETTIST_CHARACTERISTIC =
            ITEMS.register("marionettist_characteristic", () -> new Item(new Item.Properties().fireResistant()));
    public static final DeferredItem<Item> BIZARRO_SORCERER_CHARACTERISTIC =
            ITEMS.register("bizarro_sorcerer_characteristic", () -> new Item(new Item.Properties().fireResistant()));
    public static final DeferredItem<Item> SCHOLAR_OF_YORE_CHARACTERISTIC =
            ITEMS.register("scholar_of_yore_characteristic", () -> new Item(new Item.Properties().fireResistant()));
    public static final DeferredItem<Item> MIRACLE_INVOKER_CHARACTERISTIC =
            ITEMS.register("miracle_invoker_characteristic", () -> new Item(new Item.Properties().fireResistant()));
    public static final DeferredItem<Item> ATTENDANT_OF_MYSTERIES_CHARACTERISTIC =
            ITEMS.register("attendant_of_mysteries_characteristic", () -> new Item(new Item.Properties().fireResistant()));
    //endregion

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
