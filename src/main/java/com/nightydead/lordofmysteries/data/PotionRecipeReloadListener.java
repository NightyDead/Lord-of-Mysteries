package com.nightydead.lordofmysteries.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.nightydead.lordofmysteries.LordofMysteries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

/**
 * 魔药配方数据包加载器
 * 从 data/<namespace>/lordofmysteries/potion_recipes/*.json 读取配方数据
 * 加载到静态 RecipeRegistry 中供 Tooltip 和右键事件使用
 */
public class PotionRecipeReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();
    private static final String FOLDER = "lordofmysteries/potion_recipes";

    public PotionRecipeReloadListener() {
        super(GSON, FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        LordofMysteries.LOGGER.info("[PotionRecipe] Loading {} potion recipe(s) from datapacks...", map.size());

        Map<String, PotionRecipeData> loaded = new HashMap<>();
        for (var entry : map.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement json = entry.getValue();
            try {
                PotionRecipeData data = PotionRecipeData.CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(err -> LordofMysteries.LOGGER.error("[PotionRecipe] Failed to parse {}: {}", id, err))
                        .orElse(null);
                if (data != null) {
                    String key = data.pathway() + ":" + data.sequence();
                    loaded.put(key, data);
                    LordofMysteries.LOGGER.debug("[PotionRecipe] Loaded recipe: {} ({}:{})",
                            id, data.pathway(), data.sequence());
                }
            } catch (Exception e) {
                LordofMysteries.LOGGER.error("[PotionRecipe] Error loading recipe from {}: {}", id, e.getMessage());
            }
        }

        PotionRecipeRegistry.setLoadedRecipes(loaded);
        LordofMysteries.LOGGER.info("[PotionRecipe] Successfully loaded {} potion recipes.", loaded.size());
    }

    @Override
    public String getName() {
        return "Potion Recipe Loader";
    }
}
