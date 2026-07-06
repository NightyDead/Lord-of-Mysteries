package com.nightydead.lordofmysteries.datagen;

import com.nightydead.lordofmysteries.block.ModBlocks;
import com.nightydead.lordofmysteries.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

public class ModRecipesProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // 金薄荷方块 → 2个金薄荷叶（无形状配方）
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.GOLD_MINT_LEAF.get(), 2)
                .requires(ModBlocks.GOLD_MINT_HERB.get())
                .unlockedBy("has_gold_mint_herb", has(ModBlocks.GOLD_MINT_HERB.get()))
                .save(recipeOutput, "lordofmysteries:gold_mint_leaf_from_herb");

        super.buildRecipes(recipeOutput);
    }
}
