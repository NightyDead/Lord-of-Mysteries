package com.nightydead.lordofmysteries.recipe;

import com.nightydead.lordofmysteries.LordofMysteries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, LordofMysteries.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, LordofMysteries.MODID);

    // 🔮 注册魔药合成表的【类型】 (lordofmysteries:potion_brewing)
    public static final Supplier<RecipeType<PotionRecipe>> POTION_BREWING_TYPE =
            RECIPE_TYPES.register("potion_brewing", () -> new RecipeType<>() {
                @Override
                public String toString() { return "potion_brewing"; }
            });

    // 📜 注册魔药合成表的【序列化器】
    public static final Supplier<RecipeSerializer<PotionRecipe>> POTION_BREWING_SERIALIZER =
            RECIPE_SERIALIZERS.register("potion_brewing", () -> PotionRecipe.SERIALIZER);

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}