package com.nightydead.lordofmysteries.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * 动态非凡掉落修饰器 - 概率掉落 + 抢夺附魔加成
 * <p>
 * 掉落公式：最终概率 = 基础概率 × (1 + 抢夺等级 × 0.5)
 * 例：基础概率 10%，抢夺 III → 10% × 2.5 = 25%
 */
public class EntityLootModifier extends LootModifier {

    /** Codec 编解码器：用于序列化/反序列化实体掉落修饰器的配置数据 */
    public static final Supplier<MapCodec<EntityLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
                    .and(BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(m -> m.item))
                    .and(Codec.STRING.fieldOf("pathway").forGetter(m -> m.pathway))
                    .and(Codec.INT.fieldOf("sequence").forGetter(m -> m.sequence))
                    .and(Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance))
                    .apply(inst, EntityLootModifier::new)
            )
    );

    /** 掉落物品引用 */
    private final Item item;
    /** 掉落物品对应的途径 ID */
    private final String pathway;
    /** 掉落物品对应的序列号 */
    private final int sequence;
    /** 基础掉落概率 */
    private final float chance;

    /**
     * 构造实体掉落修饰器
     *
     * @param conditionsIn 战利品条件数组
     * @param item         掉落物品
     * @param pathway      途径 ID
     * @param sequence     序列号
     * @param chance       基础掉落概率
     */
    public EntityLootModifier(LootItemCondition[] conditionsIn, Item item, String pathway, int sequence, float chance) {
        super(conditionsIn);
        this.item = item;
        this.pathway = pathway;
        this.sequence = sequence;
        this.chance = chance;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // 获取抢夺附魔等级
        int lootingLevel = 0;
        if (context.hasParam(LootContextParams.ATTACKING_ENTITY)) {
            var killer = context.getParamOrNull(LootContextParams.ATTACKING_ENTITY);
            if (killer instanceof LivingEntity livingKiller) {
                ItemStack weapon = livingKiller.getMainHandItem();
                if (!weapon.isEmpty()) {
                    var holder = livingKiller.level().registryAccess()
                            .registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                            .getHolder(Enchantments.LOOTING);
                    if (holder.isPresent()) {
                        lootingLevel = EnchantmentHelper.getItemEnchantmentLevel(holder.get(), weapon);
                    }
                }
            }
        }

        // 计算最终掉落概率：基础概率 × (1 + 抢夺等级 × 0.5)
        float finalChance = this.chance * (1.0f + lootingLevel * 0.5f);

        // 概率摇号
        if (context.getRandom().nextFloat() < finalChance) {
            ItemStack stack = new ItemStack(this.item);
            stack.set(ModDataComponents.PATHWAY.get(), this.pathway);
            stack.set(ModDataComponents.SEQUENCE.get(), this.sequence);
            generatedLoot.add(stack);
        }

        return generatedLoot;
    }

    @Override
    public @NotNull MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}