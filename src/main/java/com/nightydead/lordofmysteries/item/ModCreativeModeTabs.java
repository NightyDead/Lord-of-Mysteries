package com.nightydead.lordofmysteries.item;

import com.nightydead.lordofmysteries.LordofMysteries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LordofMysteries.MODID);

    public static final Supplier<CreativeModeTab> CHARACTERISTIC_TAB =
            CREATIVE_MODE_TABS.register("characteristic_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.SEER_CHARACTERISTIC.get()))
                    .title(Component.translatable("itemGroup.characteristic_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.SEER_CHARACTERISTIC);
                        output.accept(ModItems.CLOWN_CHARACTERISTIC);
                        output.accept(ModItems.MAGICIAN_CHARACTERISTIC);
                        output.accept(ModItems.FACELESS_CHARACTERISTIC);
                        output.accept(ModItems.MARIONETTIST_CHARACTERISTIC);
                        output.accept(ModItems.BIZARRO_SORCERER_CHARACTERISTIC);
                        output.accept(ModItems.SCHOLAR_OF_YORE_CHARACTERISTIC);
                        output.accept(ModItems.MIRACLE_INVOKER_CHARACTERISTIC);
                        output.accept(ModItems.ATTENDANT_OF_MYSTERIES_CHARACTERISTIC);
                    }).build());

    public static final Supplier<CreativeModeTab> POTION_TAB =
            CREATIVE_MODE_TABS.register("potion_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.SEER_POTION.get()))
                    .title(Component.translatable("itemGroup.potion_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.SEER_POTION);
                    }).withTabsBefore(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "characteristic_tab"))
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

}
