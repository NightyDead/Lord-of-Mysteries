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

/**
 * 创造模式标签页注册类
 * 管理模组所有创造模式标签页的注册与内容填充
 * 每个标签页对应游戏中一个独立的物品分组，方便玩家浏览和查找模组物品
 */
public class ModCreativeModeTabs {

    /** 创造模式标签页延迟注册表，使用模组 ID 作为命名空间 */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LordofMysteries.MODID);

    /** 非凡特性标签页 - 包含所有序列的非凡特性和聚合特性 */
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

    /** 魔药标签页 - 包含所有途径的魔药 */
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

    /** 模组方块标签页 - 包含模组自定义方块 */
    public static final Supplier<CreativeModeTab> MOD_BLOCK_TAB =
            CREATIVE_MODE_TABS.register("mod_block_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.EXAMPLE_BLOCK.get()))
                    .title(Component.translatable("itemGroup.mod_block_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.EXAMPLE_BLOCK);
                    }).withTabsBefore(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "potion_tab"))
                    .build());

    /** 魔药主材标签页 - 包含拉瓦章鱼血液、星水晶等核心合成材料 */
    public static final Supplier<CreativeModeTab> POTION_MATERIAL_TAB =
            CREATIVE_MODE_TABS.register("potion_material_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.LAVA_OCTOPUS_BLOOD.get()))
                    .title(Component.translatable("itemGroup.potion_main_material_tab"))
                    .displayItems((parameters, output) -> {
                        // 🚀 直接接受带组件的注册单例
                        output.accept(ModItems.LAVA_OCTOPUS_BLOOD.get());
                        output.accept(ModItems.STAR_CRYSTAL.get());
                    }).withTabsBefore(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "mod_block_tab"))
                    .build());

    /** 魔药辅助材料标签页 - 包含纯水、夜香果汁等辅助合成材料 */
    public static final Supplier<CreativeModeTab> POTION_AUXILIARY_MATERIALS =
            CREATIVE_MODE_TABS.register("potion_auxiliary_materials", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.NIGHT_PERFUME_JUICE.get()))
                    .title(Component.translatable("itemGroup.potion_auxiliary_materials_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.NIGHT_PERFUME_JUICE.get());
                        output.accept(ModItems.PURE_WATER.get());
                        output.accept(ModItems.POISON_HEMLOCK_JUICE.get());
                        output.accept(ModItems.GOLD_MINT_LEAF.get());
                        output.accept(ModItems.DRAGON_BLOOD_POWDER.get());
                    }).withTabsBefore(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "potion_material_tab"))
                    .build());

    /** 自然物品标签页 - 包含四种神秘学草药方块 */
    public static final Supplier<CreativeModeTab> NATURAL_ITEM_TAB =
            CREATIVE_MODE_TABS.register("natural_item_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.NIGHT_PERFUME_HERB.get()))
                    .title(Component.translatable("itemGroup.natural_item_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.NIGHT_PERFUME_HERB.get());
                        output.accept(ModBlocks.GOLD_MINT_HERB.get());
                        output.accept(ModBlocks.POISON_HEMLOCK_HERB.get());
                        output.accept(ModBlocks.DRAGON_BLOOD_HERB.get());
                    }).withTabsBefore(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "potion_auxiliary_materials"))
                    .build());

    /**
     * 将创造模式标签页注册表绑定到模组事件总线
     *
     * @param eventBus 模组事件总线
     */
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}