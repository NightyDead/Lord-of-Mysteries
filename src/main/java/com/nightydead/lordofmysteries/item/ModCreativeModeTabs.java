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
 * 创造模式标签注册类
 * 已完美修复魔药标签页没有注入 Data Component 数据的致命 Bug
 */
public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LordofMysteries.MODID);

    /**
     * 特性标签页
     */
    public static final Supplier<CreativeModeTab> CHARACTERISTIC_TAB =
            CREATIVE_MODE_TABS.register("characteristic_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.SEER_CHARACTERISTIC.get()))
                    .title(Component.translatable("itemGroup.characteristic_tab"))
                    .displayItems((parameters, output) -> {
                        // 1. 注入通用的【凝聚的非凡特性】
                        ItemStack aggregated = new ItemStack(ModItems.AGGREGATED_CHARACTERISTIC.get());
                        List<String> defaultHistory = new ArrayList<>();
                        defaultHistory.add("fool:9");
                        defaultHistory.add("fool:8");
                        defaultHistory.add("fool:7");
                        aggregated.set(ModDataComponents.AGGREGATED_FEATURES.get(), defaultHistory);
                        output.accept(aggregated);

                        // 2. 依次注入占卜家途径独立的非凡特性（带有完整数据）
                        output.accept(createCharacteristicStack(ModItems.SEER_CHARACTERISTIC.get(), "fool", 9, 50));
                        output.accept(createCharacteristicStack(ModItems.CLOWN_CHARACTERISTIC.get(), "fool", 8, 100));
                        output.accept(createCharacteristicStack(ModItems.MAGICIAN_CHARACTERISTIC.get(), "fool", 7, 200));
                        output.accept(createCharacteristicStack(ModItems.FACELESS_CHARACTERISTIC.get(), "fool", 6, 350));
                        output.accept(createCharacteristicStack(ModItems.MARIONETTIST_CHARACTERISTIC.get(), "fool", 5, 500));
                        output.accept(createCharacteristicStack(ModItems.BIZARRO_SORCERER_CHARACTERISTIC.get(), "fool", 4, 1000));
                        output.accept(createCharacteristicStack(ModItems.SCHOLAR_OF_YORE_CHARACTERISTIC.get(), "fool", 3, 2000));
                        output.accept(createCharacteristicStack(ModItems.MIRACLE_INVOKER_CHARACTERISTIC.get(), "fool", 2, 5000));
                        output.accept(createCharacteristicStack(ModItems.ATTENDANT_OF_MYSTERIES_CHARACTERISTIC.get(), "fool", 1, 10000));
                    }).build());

    /**
     * 🔥 药水/魔药标签页
     * 修改点：现在不再是单纯注入空壳 Item，而是注入携带了完整神性数据的魔药 ItemStack！
     */
    public static final Supplier<CreativeModeTab> POTION_TAB =
            CREATIVE_MODE_TABS.register("potion_tab", () -> CreativeModeTab.builder()
                    .icon(() -> createPotionStack(ModItems.SEER_POTION.get(), "fool", 9, 50)) // 标签图标使用带有神性的魔药
                    .title(Component.translatable("itemGroup.potion_tab"))
                    .displayItems((parameters, output) -> {
                        // 🌟 在这里注入每一个魔药时，都使用我们写的辅助方法赋予其生命！
                        // 占卜家途径：序列 9「占卜家」魔药
                        output.accept(createPotionStack(ModItems.SEER_POTION.get(), "fool", 9, 50));

                        // 未来你增加了其他序列的魔药，在这里继续按规矩加：
                        // output.accept(createPotionStack(ModItems.CLOWN_POTION.get(), "fool", 8, 100));
                        // output.accept(createPotionStack(ModItems.MAGICIAN_POTION.get(), "fool", 7, 200));
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

    /**
     * ✨ 辅助方法 1：构建带有非凡数据组件的特性 ItemStack
     */
    private static ItemStack createCharacteristicStack(net.minecraft.world.item.Item item, String pathway, int sequence, int maxSpirituality) {
        ItemStack stack = new ItemStack(item);
        stack.set(ModDataComponents.PATHWAY.get(), pathway);
        stack.set(ModDataComponents.SEQUENCE.get(), sequence);
        stack.set(ModDataComponents.MAX_SPIRITUALITY.get(), maxSpirituality);
        return stack;
    }

    /**
     * 🔥 【核心新增】辅助方法 2：快速构建带有完整超凡神性组件的【魔药】ItemStack
     * @param item 注册的魔药物品对象
     * @param pathway 途径名称 (如 "fool")
     * @param sequence 对应的序列数 (9-0)
     * @param maxSpirituality 喝下后玩家可以获得的/对应的最大灵性上限
     * @return 携带完整神性编码的魔药物品堆
     */
    private static ItemStack createPotionStack(net.minecraft.world.item.Item item, String pathway, int sequence, int maxSpirituality) {
        ItemStack stack = new ItemStack(item);
        stack.set(ModDataComponents.PATHWAY.get(), pathway);
        stack.set(ModDataComponents.SEQUENCE.get(), sequence);
        stack.set(ModDataComponents.MAX_SPIRITUALITY.get(), maxSpirituality);
        return stack;
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}