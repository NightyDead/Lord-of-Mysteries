package com.nightydead.lordofmysteries.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.data.PotionRecipeData;
import com.nightydead.lordofmysteries.data.PotionRecipeRegistry;
import com.nightydead.lordofmysteries.item.ModItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * 箱子战利品修饰器 —— 在指定结构的箱子中概率掉落魔药配方
 * <p>
 * 从 PotionRecipeRegistry 已加载的配方中随机选取一个，以设定概率注入箱子战利品。
 * 每个箱子最多注入 1 个配方（由 GLM 匹配机制保证：每个箱子战利品表只匹配一次）。
 */
public class ChestLootModifier extends LootModifier {

    /** Codec 编解码器：序列化/反序列化箱子战利品修饰器配置 */
    public static final Supplier<MapCodec<ChestLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
                    .and(Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance))
                    .apply(inst, ChestLootModifier::new)
            )
    );

    /** 配方掉落概率（0.0~1.0） */
    private final float chance;

    /**
     * 构造箱子战利品修饰器
     *
     * @param conditionsIn 战利品条件数组（通常为 loot_table_id 匹配）
     * @param chance       配方掉落概率
     */
    public ChestLootModifier(LootItemCondition[] conditionsIn, float chance) {
        super(conditionsIn);
        this.chance = chance;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // 检查是否有可用配方
        List<String> keys = PotionRecipeRegistry.getAllKeys();
        if (keys.isEmpty()) {
            return generatedLoot;
        }

        // 概率摇号
        if (context.getRandom().nextFloat() < this.chance) {
            // 随机选取一个已加载的配方
            String key = keys.get(context.getRandom().nextInt(keys.size()));
            PotionRecipeData data = PotionRecipeRegistry.get(
                    key.split(":")[0],
                    Integer.parseInt(key.split(":")[1])
            );
            if (data != null) {
                ItemStack recipeStack = new ItemStack(ModItems.POTION_RECIPE.get(), 1);
                recipeStack.set(ModDataComponents.RECIPE_DATA.get(), data);
                generatedLoot.add(recipeStack);
            }
        }

        return generatedLoot;
    }

    @Override
    public @NotNull MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
