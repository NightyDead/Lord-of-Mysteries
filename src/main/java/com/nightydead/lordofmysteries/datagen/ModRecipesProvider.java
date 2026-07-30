package com.nightydead.lordofmysteries.datagen;

import com.nightydead.lordofmysteries.block.ModBlocks;
import com.nightydead.lordofmysteries.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * 配方数据提供者
 * 负责为模组物品生成合成配方 JSON 文件
 * 实现 IConditionBuilder 接口以支持条件配方的构建
 */
public class ModRecipesProvider extends RecipeProvider implements IConditionBuilder {

    /**
     * 构造配方提供者
     *
     * @param output     数据包输出目录
     * @param registries 注册表查找器的异步Future
     */
    public ModRecipesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    /**
     * 注册所有合成配方
     * 当前包含草药分解为对应材料的无形状配方和纯水烧制配方
     *
     * @param recipeOutput 配方输出接口，用于保存生成的配方
     */
    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // 金薄荷方块 → 2个金薄荷叶（无形状配方）
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.GOLD_MINT_LEAF.get(), 2)
                .requires(ModBlocks.GOLD_MINT_HERB.get())
                .unlockedBy("has_gold_mint_herb", has(ModBlocks.GOLD_MINT_HERB.get()))
                .save(recipeOutput, "lordofmysteries:gold_mint_leaf_from_herb");

        // 龙血草方块 → 3个龙血草粉末（无形状配方）
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.DRAGON_BLOOD_POWDER.get(), 3)
                .requires(ModBlocks.DRAGON_BLOOD_HERB.get())
                .unlockedBy("has_dragon_blood_herb", has(ModBlocks.DRAGON_BLOOD_HERB.get()))
                .save(recipeOutput, "lordofmysteries:dragon_blood_powder_from_herb");

        // 水瓶 → 纯水（熔炉烧制，200 tick）
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(Items.POTION),
                        RecipeCategory.MISC,
                        ModItems.PURE_WATER.get(),
                        0.1f,
                        200)
                .unlockedBy("has_water_bottle", has(Items.POTION))
                .save(recipeOutput, "lordofmysteries:pure_water_from_smelting");

        // 水瓶 → 纯水（烟熏炉烧制，100 tick）
        SimpleCookingRecipeBuilder.smoking(
                        Ingredient.of(Items.POTION),
                        RecipeCategory.MISC,
                        ModItems.PURE_WATER.get(),
                        0.1f,
                        100)
                .unlockedBy("has_water_bottle", has(Items.POTION))
                .save(recipeOutput, "lordofmysteries:pure_water_from_smoking");

        // 仪式匕首：铁锭 + 花卉 + 木棍（竖直一列）
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RITUAL_DAGGER.get())
                .pattern("I")
                .pattern("F")
                .pattern("S")
                .define('I', Items.IRON_INGOT)
                .define('F', ItemTags.FLOWERS)
                .define('S', Items.STICK)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(recipeOutput);

        //炼药锅：原版炼药锅 +8个黑石围一圈
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ALCHEMY_CAULDRON.get())
                .pattern("BBB")
                .pattern("BCB")
                .pattern("BBB")
                .define('B', Items.BLACKSTONE)
                .define('C', Items.CAULDRON)
                .unlockedBy("has_cauldron", has(Items.CAULDRON))
                .save(recipeOutput);

        super.buildRecipes(recipeOutput);
    }
}
