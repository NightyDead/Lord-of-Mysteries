package com.nightydead.lordofmysteries.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * 魔药合成配方类
 * 定义魔药炼制的完整配方结构，包括辅助材料（无序）和主材料（严格时序）
 * 支持 Codec 序列化/反序列化和网络流同步
 */
public class PotionRecipe implements Recipe<SingleRecipeInput> {

    /** Codec 编解码器：支持辅助材料（无序成分）与主材料（严格时序）的序列化 */
    public static final MapCodec<PotionRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.listOf().fieldOf("auxiliary_ingredients").forGetter(r -> r.auxiliaryIngredients),
            Ingredient.CODEC.listOf().fieldOf("main_ingredients_order").forGetter(r -> r.mainIngredientsOrder),
            Codec.STRING.fieldOf("pathway").forGetter(r -> r.pathway),
            Codec.INT.fieldOf("sequence").forGetter(r -> r.sequence)
    ).apply(inst, PotionRecipe::new));

    /** 网络流同步编解码器：使用原版 Ingredient 的 StreamCodec 进行网络传输 */
    public static final StreamCodec<RegistryFriendlyByteBuf, PotionRecipe> STREAM_CODEC = StreamCodec.of(
            (buf, recipe) -> {
                buf.writeVarInt(recipe.auxiliaryIngredients.size());
                for (Ingredient ing : recipe.auxiliaryIngredients) Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ing);
                buf.writeVarInt(recipe.mainIngredientsOrder.size());
                for (Ingredient ing : recipe.mainIngredientsOrder) Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ing);
                buf.writeUtf(recipe.pathway);
                buf.writeInt(recipe.sequence);
            },
            buf -> {
                int auxSize = buf.readVarInt();
                List<Ingredient> aux = new java.util.ArrayList<>(auxSize);
                for (int i = 0; i < auxSize; i++) aux.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                int mainSize = buf.readVarInt();
                List<Ingredient> main = new java.util.ArrayList<>(mainSize);
                for (int i = 0; i < mainSize; i++) main.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                return new PotionRecipe(aux, main, buf.readUtf(), buf.readInt());
            }
    );

    /** 配方序列化器实例，整合 Codec 和 StreamCodec */
    public static final RecipeSerializer<PotionRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override public MapCodec<PotionRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, PotionRecipe> streamCodec() { return STREAM_CODEC; }
    };

    /** 辅助材料列表（无序，可任意顺序投入） */
    private final List<Ingredient> auxiliaryIngredients;
    /** 主材料列表（严格时序，必须按顺序投入） */
    private final List<Ingredient> mainIngredientsOrder;
    /** 配方对应的途径 ID */
    private final String pathway;
    /** 配方对应的序列号 */
    private final int sequence;

    /**
     * 构造魔药配方
     *
     * @param auxiliaryIngredients   辅助材料列表（无序）
     * @param mainIngredientsOrder   主材料列表（严格时序）
     * @param pathway                配方对应的途径 ID
     * @param sequence               配方对应的序列号
     */
    public PotionRecipe(List<Ingredient> auxiliaryIngredients, List<Ingredient> mainIngredientsOrder, String pathway, int sequence) {
        this.auxiliaryIngredients = auxiliaryIngredients;
        this.mainIngredientsOrder = mainIngredientsOrder;
        this.pathway = pathway;
        this.sequence = sequence;
    }

    /** 获取辅助材料列表 */
    public List<Ingredient> getAuxiliaryIngredients() { return auxiliaryIngredients; }
    /** 获取主材料列表 */
    public List<Ingredient> getMainIngredientsOrder() { return mainIngredientsOrder; }
    /** 获取配方对应的途径 ID */
    public String getPathway() { return pathway; }
    /** 获取配方对应的序列号 */
    public int getSequence() { return sequence; }

    @Override public boolean matches(@NotNull SingleRecipeInput input, @NotNull Level level) { return false; }
    @Override public @NotNull ItemStack assemble(@NotNull SingleRecipeInput input, @NotNull HolderLookup.Provider registries) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider registries) { return ItemStack.EMPTY; }
    @Override public @NotNull RecipeSerializer<?> getSerializer() { return SERIALIZER; }
    @Override public @NotNull RecipeType<?> getType() { return ModRecipes.POTION_BREWING_TYPE.get(); }
}