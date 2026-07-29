package com.nightydead.lordofmysteries.event;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nightydead.lordofmysteries.LordofMysteries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 酿造台配方注册 —— 数据驱动版本
 * <p>
 * 配方定义在 {@code data/lordofmysteries/lordofmysteries/brewing_recipes.json} 中，
 * 添加新配方只需在该 JSON 文件中追加条目，无需修改此 Java 文件，无需重编译。
 */
@EventBusSubscriber(modid = LordofMysteries.MODID)
public class ModBrewingRecipes {

    private static final Gson GSON = new Gson();
    private static final String RECIPES_PATH = "/data/lordofmysteries/lordofmysteries/brewing_recipes.json";

    /**
     * 从 bundled JSON 读取配方并注册到酿造台
     */
    @SubscribeEvent
    public static void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        var builder = event.getBuilder();

        List<BrewingRecipeEntry> entries;
        try (var in = ModBrewingRecipes.class.getResourceAsStream(RECIPES_PATH)) {
            if (in == null) {
                LordofMysteries.LOGGER.warn("[Brewing] Recipe file not found: {}", RECIPES_PATH);
                return;
            }
            entries = GSON.fromJson(
                    new InputStreamReader(in, StandardCharsets.UTF_8),
                    new TypeToken<List<BrewingRecipeEntry>>() {}.getType()
            );
        } catch (Exception e) {
            LordofMysteries.LOGGER.error("[Brewing] Failed to load brewing recipes: {}", e.getMessage());
            return;
        }

        if (entries == null || entries.isEmpty()) {
            LordofMysteries.LOGGER.warn("[Brewing] No brewing recipes defined.");
            return;
        }

        for (BrewingRecipeEntry entry : entries) {
            try {
                var inputItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(entry.input));
                var ingredientItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(entry.ingredient));
                var outputItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(entry.output));
                builder.addRecipe(new BrewingRecipe(
                        Ingredient.of(inputItem),
                        Ingredient.of(ingredientItem),
                        new ItemStack(outputItem)
                ));
                LordofMysteries.LOGGER.info("[Brewing] Registered: {} + {} → {}",
                        entry.input, entry.ingredient, entry.output);
            } catch (Exception e) {
                LordofMysteries.LOGGER.error("[Brewing] Failed to register recipe {}: {}", entry, e.getMessage());
            }
        }
    }

    /** JSON 配方条目映射 */
    @SuppressWarnings("unused")
    private static class BrewingRecipeEntry {
        String input;
        String ingredient;
        String output;
    }
}
