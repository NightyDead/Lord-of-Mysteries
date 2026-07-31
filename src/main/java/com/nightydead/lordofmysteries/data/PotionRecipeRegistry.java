package com.nightydead.lordofmysteries.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.nightydead.lordofmysteries.LordofMysteries;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 魔药配方注册表（运行时缓存）
 * 由类路径加载器或数据包重载填充，提供通过 pathway:sequence 键查找完整配方数据的能力
 */
public class PotionRecipeRegistry {

    /** pathway:sequence → 完整配方数据 */
    private static Map<String, PotionRecipeData> recipes = new HashMap<>();
    private static boolean loaded = false;

    /** 由数据包重载监听器调用，更新已加载的配方数据 */
    public static void setLoadedRecipes(Map<String, PotionRecipeData> loaded) {
        recipes = loaded;
    }

    /**
     * 从类路径加载内置魔药配方 JSON 文件
     * 扫描 data/lordofmysteries/lordofmysteries/potion_recipes/ 目录
     */
    public static void loadBuiltin() {
        if (loaded) return;
        loaded = true;

        Map<String, PotionRecipeData> builtin = new HashMap<>();
        // 手动列出所有内置配方文件
        String[] builtinFiles = {"fool_9", "fool_8"};
        for (String fileName : builtinFiles) {
            String path = "/data/lordofmysteries/lordofmysteries/potion_recipes/" + fileName + ".json";
            try (var in = PotionRecipeRegistry.class.getResourceAsStream(path)) {
                if (in == null) {
                    LordofMysteries.LOGGER.warn("[PotionRecipe] Builtin recipe not found: {}", path);
                    continue;
                }
                JsonElement json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                PotionRecipeData data = PotionRecipeData.CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(err -> LordofMysteries.LOGGER.error("[PotionRecipe] Parse error {}: {}", fileName, err))
                        .orElse(null);
                if (data != null) {
                    String key = data.pathway() + ":" + data.sequence();
                    builtin.put(key, data);
                    LordofMysteries.LOGGER.info("[PotionRecipe] Loaded builtin recipe: {}:{}", data.pathway(), data.sequence());
                }
            } catch (Exception e) {
                LordofMysteries.LOGGER.error("[PotionRecipe] Failed to load builtin {}: {}", fileName, e.getMessage());
            }
        }
        recipes = builtin;
        LordofMysteries.LOGGER.info("[PotionRecipe] Loaded {} builtin potion recipes.", builtin.size());
    }

    /**
     * 根据途径和序列查找已加载的完整配方数据
     *
     * @param pathway  途径 ID
     * @param sequence 序列号
     * @return 完整配方数据，未加载则返回 null
     */
    public static PotionRecipeData get(String pathway, int sequence) {
        return recipes.get(pathway + ":" + sequence);
    }

    /** 获取已加载的配方总数 */
    public static int size() {
        return recipes.size();
    }
}
