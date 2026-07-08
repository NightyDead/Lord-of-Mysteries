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
     * 当前包含金薄荷方块分解为金薄荷叶的无形状配方
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

        super.buildRecipes(recipeOutput);
    }
}
