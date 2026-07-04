package com.nightydead.lordofmysteries.item;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.block.ModBlocks;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LordofMysteries.MODID);

    public static final Supplier<CreativeModeTab> CHARACTERISTIC_TAB =
            CREATIVE_MODE_TABS.register("characteristic_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.SEER_CHARACTERISTIC.get()))
                    .title(Component.translatable("itemGroup.characteristic_tab"))
                    .displayItems((parameters, output) -> {
                        // 1. 注入聚合非凡特性（保持你原有的三条预置记录即可）
                        ItemStack aggregated = new ItemStack(ModItems.AGGREGATED_CHARACTERISTIC.get());
                        List<String> defaultHistory = new ArrayList<>();
                        defaultHistory.add("fool:9");
                        defaultHistory.add("fool:8");
                        defaultHistory.add("fool:7");
                        aggregated.set(ModDataComponents.AGGREGATED_FEATURES.get(), defaultHistory);
                        output.accept(aggregated);

                        // 2. 🚀 极致简化：直接把注册表里的单例塞进创造栏，它们出厂便自带组件！
                        output.accept(ModItems.SEER_CHARACTERISTIC.get());
                        output.accept(ModItems.CLOWN_CHARACTERISTIC.get());
                        output.accept(ModItems.MAGICIAN_CHARACTERISTIC.get());
                        output.accept(ModItems.FACELESS_CHARACTERISTIC.get());
                        output.accept(ModItems.MARIONETTIST_CHARACTERISTIC.get());
                        output.accept(ModItems.BIZARRO_SORCERER_CHARACTERISTIC.get());
                        output.accept(ModItems.SCHOLAR_OF_YORE_CHARACTERISTIC.get());
                        output.accept(ModItems.MIRACLE_INVOKER_CHARACTERISTIC.get());
                        output.accept(ModItems.ATTENDANT_OF_MYSTERIES_CHARACTERISTIC.get());
                    }).build());

    public static final Supplier<CreativeModeTab> POTION_TAB =
            CREATIVE_MODE_TABS.register("potion_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.SEER_POTION.get()))
                    .title(Component.translatable("itemGroup.potion_tab"))
                    .displayItems((parameters, output) -> {
                        // 🚀 直接接受带组件的注册单例
                        output.accept(ModItems.SEER_POTION.get());
                    })
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "characteristic_tab"))
                    .build());

    public static final Supplier<CreativeModeTab> MOD_BLOCK_TAB =
            CREATIVE_MODE_TABS.register("mod_block_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.EXAMPLE_BLOCK.get()))
                    .title(Component.translatable("itemGroup.mod_block_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.EXAMPLE_BLOCK);
                    }).withTabsBefore(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "potion_tab"))
                    .build());

    public static final Supplier<CreativeModeTab> POTION_MATERIAL_TAB =
            CREATIVE_MODE_TABS.register("potion_material_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.MAIN_EYE_OF_A_LAVOS_SQUID.get()))
                    .title(Component.translatable("itemGroup.potion_material_tab"))
                    .displayItems((parameters, output) -> {
                        // 🚀 直接接受带组件的注册单例
                        output.accept(ModItems.MAIN_EYE_OF_A_LAVOS_SQUID.get());
                    }).withTabsBefore(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "mod_block_tab"))
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}