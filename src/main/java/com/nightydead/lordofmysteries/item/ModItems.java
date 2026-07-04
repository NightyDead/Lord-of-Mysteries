package com.nightydead.lordofmysteries.item;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.item.custom.CharacteristicItem;
import com.nightydead.lordofmysteries.item.custom.PotionItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 模组物品注册类
 * 统一管理并动态挂载超凡核心组件（数据基因）
 */
public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LordofMysteries.MODID);

    // ==================== 聚合非凡特性 ====================
    public static final DeferredItem<Item> AGGREGATED_CHARACTERISTIC =
            ITEMS.register("characteristic/aggregated_characteristic", () -> new CharacteristicItem());
    // ==================== 占卜家途径非凡特性（序列 9 ~ 序列 1） ====================
    public static final DeferredItem<Item> SEER_CHARACTERISTIC = registerCharacteristic("seer_characteristic", "fool", 9, 50);
    public static final DeferredItem<Item> MAIN_EYE_OF_A_LAVOS_SQUID = registerCharacteristic("main_eye_of_a_lavos_squid", "fool", 9, 50); // 占卜家途径主材料
    public static final DeferredItem<Item> CLOWN_CHARACTERISTIC = registerCharacteristic("clown_characteristic", "fool", 8, 100);
    public static final DeferredItem<Item> MAGICIAN_CHARACTERISTIC = registerCharacteristic("magician_characteristic", "fool", 7, 200);
    public static final DeferredItem<Item> FACELESS_CHARACTERISTIC = registerCharacteristic("faceless_characteristic", "fool", 6, 350);
    public static final DeferredItem<Item> MARIONETTIST_CHARACTERISTIC = registerCharacteristic("marionettist_characteristic", "fool", 5, 500);
    public static final DeferredItem<Item> BIZARRO_SORCERER_CHARACTERISTIC = registerCharacteristic("bizarro_sorcerer_characteristic", "fool", 4, 1000);
    public static final DeferredItem<Item> SCHOLAR_OF_YORE_CHARACTERISTIC = registerCharacteristic("scholar_of_yore_characteristic", "fool", 3, 2000);
    public static final DeferredItem<Item> MIRACLE_INVOKER_CHARACTERISTIC = registerCharacteristic("miracle_invoker_characteristic", "fool", 2, 5000);
    public static final DeferredItem<Item> ATTENDANT_OF_MYSTERIES_CHARACTERISTIC = registerCharacteristic("attendant_of_mysteries_characteristic", "fool", 1, 10000);

    // ==================== 占卜家途径魔药 ====================
    public static final DeferredItem<Item> SEER_POTION = ITEMS.register("potion/seer_potion",
            () -> new PotionItem(PotionItem.createDefaultProperties()
                    .component(ModDataComponents.PATHWAY.get(), "fool")
                    .component(ModDataComponents.SEQUENCE.get(), 9)
                    .component(ModDataComponents.MAX_SPIRITUALITY.get(), 50)));

    /**
     * 🧠 工业级封装快捷注册函数：全自动注入特定途径、序列与灵性组件
     */
    private static DeferredItem<Item> registerCharacteristic(String name, String pathway, int sequence, int maxSpiritual) {
        return ITEMS.register("characteristic/" + name, () -> new CharacteristicItem(
                CharacteristicItem.createDefaultProperties()
                        .component(ModDataComponents.PATHWAY.get(), pathway)
                        .component(ModDataComponents.SEQUENCE.get(), sequence)
                        .component(ModDataComponents.MAX_SPIRITUALITY.get(), maxSpiritual)
        ));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}