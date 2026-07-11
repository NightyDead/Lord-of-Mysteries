package com.nightydead.lordofmysteries.event;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.block.ModBlocks;
import com.nightydead.lordofmysteries.item.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

/**
 * 酿造台配方注册事件处理器
 * 负责注册模组自定义酿造台配方（水瓶 + 草药 → 辅助材料）
 * 通过 NeoForge 的 IBrewingRecipe 系统实现非药水类的酿造配方
 */
@EventBusSubscriber(modid = LordofMysteries.MODID)
public class ModBrewingRecipes {

    /**
     * 注册酿造台配方
     * 在 RegisterBrewingRecipesEvent 事件中添加自定义酿造配方
     * 使用 BrewingRecipe（Ingredient input, Ingredient ingredient, ItemStack output）构造
     *
     * @param event 酿造配方注册事件
     */
    @SubscribeEvent
    public static void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        var builder = event.getBuilder();

        // 水瓶 + 夜香草 → 夜香草汁液
        builder.addRecipe(new BrewingRecipe(
                Ingredient.of(Items.POTION),
                Ingredient.of(ModBlocks.NIGHT_PERFUME_HERB.get()),
                new ItemStack(ModItems.NIGHT_PERFUME_JUICE.get())
        ));

        // 水瓶 + 毒堇 → 毒堇汁
        builder.addRecipe(new BrewingRecipe(
                Ingredient.of(Items.POTION),
                Ingredient.of(ModBlocks.POISON_HEMLOCK_HERB.get()),
                new ItemStack(ModItems.POISON_HEMLOCK_JUICE.get())
        ));
    }
}
