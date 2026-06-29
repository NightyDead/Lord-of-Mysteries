package com.nightydead.lordofmysteries.item;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 创造模式标签注册类
 * 用于在创造模式物品栏中创建自定义标签页
 */
public class ModCreativeModeTabs {
    /**
     * 创造模式标签的延迟注册器
     * 使用DeferredRegister进行延迟注册，确保在正确的时机注册
     */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LordofMysteries.MODID);

    /**
     * 特性标签的供应器
     * 创建一个包含所有角色特性物品的创造模式标签页
     */
    public static final Supplier<CreativeModeTab> CHARACTERISTIC_TAB =
            CREATIVE_MODE_TABS.register("characteristic_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.SEER_CHARACTERISTIC.get()))  // 设置标签页图标
                    .title(Component.translatable("itemGroup.characteristic_tab"))  // 设置标签页标题
                    .displayItems((parameters, output) -> {  // 设置标签页显示的物品列表
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

    /**
     * 药水标签的供应器
     * 创建一个包含药水物品的创造模式标签页
     * 此标签页会显示在特性标签页之前
     */
    public static final Supplier<CreativeModeTab> POTION_TAB =
            CREATIVE_MODE_TABS.register("potion_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.SEER_POTION.get()))  // 设置标签页图标
                    .title(Component.translatable("itemGroup.potion_tab"))  // 设置标签页标题
                    .displayItems((parameters, output) -> {  // 设置标签页显示的物品列表
                        output.accept(ModItems.SEER_POTION);
                    }).withTabsBefore(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "characteristic_tab"))  // 设置此标签页显示在特性标签页之前
                    .build());

    public static final Supplier<CreativeModeTab> MOD_BLOCK_TAB =
            CREATIVE_MODE_TABS.register("mod_block_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.EXAMPLE_BLOCK.get()))  // 设置标签页图标
                    .title(Component.translatable("itemGroup.mod_block_tab"))  // 设置标签页标题
                    .displayItems((parameters, output) -> {  // 设置标签页显示的物品列表
                        output.accept(ModBlocks.EXAMPLE_BLOCK);
                    }).withTabsBefore(ResourceLocation.fromNamespaceAndPath(LordofMysteries.MODID, "potion_tab"))  // 设置此标签页显示在药水标签页之前
                    .build());

    /**
     * 注册创造模式标签到事件总线
     * @param eventBus 事件总线，用于注册创造模式标签
     */
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

}
