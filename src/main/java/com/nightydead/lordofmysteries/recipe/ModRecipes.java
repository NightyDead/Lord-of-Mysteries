package com.nightydead.lordofmysteries.recipe;

import com.nightydead.lordofmysteries.LordofMysteries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 模组配方注册类
 * 负责注册魔药合成表的配方类型和序列化器
 * 使用 DeferredRegister 系统确保配方在游戏加载时正确注册
 */
public class ModRecipes {

    /** 配方类型延迟注册表 */
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, LordofMysteries.MODID);

    /** 配方序列化器延迟注册表 */
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, LordofMysteries.MODID);

    /** 魔药合成配方类型 - 注册键名为 "lordofmysteries:potion_brewing" */
    public static final Supplier<RecipeType<PotionRecipe>> POTION_BREWING_TYPE =
            RECIPE_TYPES.register("potion_brewing", () -> new RecipeType<>() {
                @Override
                public String toString() { return "potion_brewing"; }
            });

    /** 魔药合成配方序列化器 - 用于从 JSON 读取和网络同步配方数据 */
    public static final Supplier<RecipeSerializer<PotionRecipe>> POTION_BREWING_SERIALIZER =
            RECIPE_SERIALIZERS.register("potion_brewing", () -> PotionRecipe.SERIALIZER);

    /**
     * 将配方注册表绑定到模组事件总线
     *
     * @param eventBus 模组事件总线
     */
    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}