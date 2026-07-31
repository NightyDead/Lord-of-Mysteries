package com.nightydead.lordofmysteries.item;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.block.ModBlocks;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.data.PotionRecipeData;
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
                    .icon(() -> new ItemStack(ModItems.getPureCharacteristic("fool", 9)))
                    .title(Component.translatable("itemGroup.characteristic_tab"))
                    .displayItems((parameters, output) -> {
                        // 1. 注入聚合非凡特性
                        ItemStack aggregated = new ItemStack(ModItems.AGGREGATED_CHARACTERISTIC.get());
                        List<String> defaultHistory = new ArrayList<>();
                        defaultHistory.add("fool:9");
                        defaultHistory.add("fool:8");
                        defaultHistory.add("fool:7");
                        aggregated.set(ModDataComponents.AGGREGATED_FEATURES.get(), defaultHistory);
                        output.accept(aggregated);

                        // 2. 批量添加全途径非凡特性
                        for (var pwEntry : ModItems.CHARACTERISTIC_MAP.entrySet()) {
                            for (var seqEntry : pwEntry.getValue().entrySet()) {
                                output.accept(seqEntry.getValue().get());
                            }
                        }
                    }).build());

    /** 魔药标签页 - 包含所有途径的魔药 */
    public static final Supplier<CreativeModeTab> POTION_TAB =
            CREATIVE_MODE_TABS.register("potion_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.getPurePotion("fool", 9)))
                    .title(Component.translatable("itemGroup.potion_tab"))
                    .displayItems((parameters, output) -> {
                        // 批量添加全途径魔药
                        for (var pwEntry : ModItems.POTION_MAP.entrySet()) {
                            for (var seqEntry : pwEntry.getValue().entrySet()) {
                                output.accept(seqEntry.getValue().get());
                            }
                        }
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
                        output.accept(ModBlocks.ALCHEMY_CAULDRON);
                        output.accept(ModBlocks.RITUAL_ALTAR);
                        output.accept(ModItems.RITUAL_DAGGER);
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
                        output.accept(ModItems.ADULT_HORNACIS_GRAY_GOAT_UNICORN_CRYSTAL.get());
                        output.accept(ModItems.COMPLETE_HUMAN_FACE_ROSE.get());
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
                        output.accept(ModItems.MANDRAKE_JUICE.get());
                        output.accept(ModItems.BLACK_EDGED_SUNFLOWER_POWDER.get());
                        output.accept(ModItems.GOLDEN_CLOAK_GRASS_POWDER.get());
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
                        output.accept(ModBlocks.MANDRAKE_HERB.get());
                        output.accept(ModBlocks.BLACK_EDGED_SUNFLOWER.get());
                        output.accept(ModBlocks.GOLDEN_CLOAK_GRASS.get());
                        output.accept(ModItems.MYSTIC_DUST.get());
                        output.accept(ModItems.KNOWLEDGE_VESSEL.get());
                    }).withTabsBefore(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "potion_auxiliary_materials"))
                    .build());

    /** 全部 22 条途径的 ID 列表（与 22途径.md 一一对应） */
    private static final String[] ALL_PATHWAY_IDS = {
            "fool", "error", "door", "visionary", "hanged_man",
            "tyrant", "sun", "white_tower", "hermit", "paragon",
            "darkness", "death", "twilight_giant", "red_priest", "demoness",
            "abyss", "chained", "moon", "mother", "justiciar",
            "black_emperor", "wheel_of_fortune"
    };

    /** 魔药配方标签页 - 包含全部 22 条途径 × 10 个序列 = 220 张配方纸 */
    public static final Supplier<CreativeModeTab> RECIPE_TAB =
            CREATIVE_MODE_TABS.register("recipe_tab", () -> CreativeModeTab.builder()
                    .icon(() -> {
                        ItemStack icon = new ItemStack(ModItems.POTION_RECIPE.get());
                        icon.set(ModDataComponents.RECIPE_DATA.get(), PotionRecipeData.simple("fool", 9));
                        return icon;
                    })
                    .title(Component.translatable("itemGroup.recipe_tab"))
                    .displayItems((parameters, output) -> {
                        for (String pathwayId : ALL_PATHWAY_IDS) {
                            for (int seq = 9; seq >= 0; seq--) {
                                ItemStack stack = new ItemStack(ModItems.POTION_RECIPE.get());
                                stack.set(ModDataComponents.RECIPE_DATA.get(),
                                        PotionRecipeData.simple(pathwayId, seq));
                                output.accept(stack);
                            }
                        }
                    }).withTabsBefore(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "natural_item_tab"))
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