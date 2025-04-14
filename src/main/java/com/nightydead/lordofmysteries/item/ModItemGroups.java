package com.nightydead.lordofmysteries.item;

import com.nightydead.lordofmysteries.LordOfMysteries;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
//    public static final RegistryKey<ItemGroup> LORD_GROUP = register("lord_group");
//    private static RegistryKey<ItemGroup> register(String id) {
//        return RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(LordOfMysteries.MOD_ID, id));
//    }
//    public static void registerModItemGroups() {
//        Registry.register(Registries.ITEM_GROUP, LORD_GROUP,
//                ItemGroup.create(ItemGroup.Row.TOP, 7)
//                        .displayName(Text.translatable("itemGroup.lord_grop"))
//                        .icon(() -> new ItemStack(ModItems.ICEICE))
//                        .entries((displayContext, entries) -> {
//                            entries.add(ModItems.ICEICE);
//                        }).build());
//        LordOfMysteries.LOGGER.info("Registering Mod Item Groups");
//    }
    public static final ItemGroup MOYAO_GROUP = Registry.register(Registries.ITEM_GROUP, Identifier.of(LordOfMysteries.MOD_ID, "moyao_group"),
        ItemGroup.create(null,-1).displayName(Text.translatable("itemGroup.moyao_group"))
                .icon(() -> new ItemStack(ModItems.MOYAO))
                .entries((displayContext, entries) -> {
                    entries.add(ModItems.MOYAO);
                }).build());

    public static void registerModItemGroups() {
        LordOfMysteries.LOGGER.info("Registering Mod Item Groups");
    }
}
