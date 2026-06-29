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
            ITEMS.register("characteristic/seer_characteristic", Characteristic::new);
    public static final DeferredItem<Item> CLOWN_CHARACTERISTIC =
            ITEMS.register("characteristic/clown_characteristic", Characteristic::new);
    public static final DeferredItem<Item> MAGICIAN_CHARACTERISTIC =
            ITEMS.register("characteristic/magician_characteristic", Characteristic::new);
    public static final DeferredItem<Item> FACELESS_CHARACTERISTIC =
            ITEMS.register("characteristic/faceless_characteristic", Characteristic::new);
    public static final DeferredItem<Item> MARIONETTIST_CHARACTERISTIC =
            ITEMS.register("characteristic/marionettist_characteristic", Characteristic::new);
    public static final DeferredItem<Item> BIZARRO_SORCERER_CHARACTERISTIC =
            ITEMS.register("characteristic/bizarro_sorcerer_characteristic", Characteristic::new);
    public static final DeferredItem<Item> SCHOLAR_OF_YORE_CHARACTERISTIC =
            ITEMS.register("characteristic/scholar_of_yore_characteristic", Characteristic::new);
    public static final DeferredItem<Item> MIRACLE_INVOKER_CHARACTERISTIC =
            ITEMS.register("characteristic/miracle_invoker_characteristic", Characteristic::new);
    public static final DeferredItem<Item> ATTENDANT_OF_MYSTERIES_CHARACTERISTIC =
            ITEMS.register("characteristic/attendant_of_mysteries_characteristic", Characteristic::new);
    //endregion

    //魔药
    //region 占卜家途径
    public static final DeferredItem<Item> SEER_POTION =
            ITEMS.register("potion/seer_potion", () -> new Item(new Item.Properties().fireResistant()));
    //endregion

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
