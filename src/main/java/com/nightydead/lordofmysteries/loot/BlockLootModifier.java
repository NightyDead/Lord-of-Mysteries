package com.nightydead.lordofmysteries.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
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
 * 动态非凡方块掉落修饰器 - 概率掉落 + 时运附魔加成
 */
public class BlockLootModifier extends LootModifier {

    /** Codec 编解码器：用于序列化/反序列化方块掉落修饰器的配置数据 */
    public static final Supplier<MapCodec<BlockLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst)
                    .and(BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(m -> m.item))
                    .and(Codec.STRING.fieldOf("pathway").forGetter(m -> m.pathway))
                    .and(Codec.INT.fieldOf("sequence").forGetter(m -> m.sequence))
                    .and(Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance))
                    .and(Codec.BOOL.optionalFieldOf("require_full_moon", false).forGetter(m -> m.requireFullMoon))
                    .apply(inst, BlockLootModifier::new)
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
    /** 是否仅在满月时掉落 */
    private final boolean requireFullMoon;

    /**
     * 构造方块掉落修饰器
     *
     * @param conditionsIn 战利品条件数组
     * @param item         掉落物品
     * @param pathway      途径 ID
     * @param sequence     序列号
     * @param chance       基础掉落概率
     */
    public BlockLootModifier(LootItemCondition[] conditionsIn, Item item, String pathway, int sequence, float chance) {
        this(conditionsIn, item, pathway, sequence, chance, false);
    }

    /**
     * 构造方块掉落修饰器（满月条件可选）
     */
    public BlockLootModifier(LootItemCondition[] conditionsIn, Item item, String pathway, int sequence, float chance, boolean requireFullMoon) {
        super(conditionsIn);
        this.item = item;
        this.pathway = pathway;
        this.sequence = sequence;
        this.chance = chance;
        this.requireFullMoon = requireFullMoon;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // 满月条件检查：若 requireFullMoon 为 true，仅满月时掉落
        if (this.requireFullMoon && context.getLevel() != null) {
            long dayTime = context.getLevel().getDayTime();
            int moonPhase = (int) ((dayTime / 24000L) % 8L);
            if (moonPhase != 0) {
                return generatedLoot;
            }
        }

        int fortuneLevel = 0;

        if (context.hasParam(LootContextParams.TOOL)) {
            ItemStack tool = context.getParam(LootContextParams.TOOL);
            if (!tool.isEmpty()) {
                var enchantmentRegistry = context.getLevel().registryAccess()
                        .registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);

                // 精准采集：不额外掉落非凡材料（玩家直接获得原方块）
                var silkTouchHolder = enchantmentRegistry.getHolder(Enchantments.SILK_TOUCH);
                if (silkTouchHolder.isPresent()
                        && EnchantmentHelper.getItemEnchantmentLevel(silkTouchHolder.get(), tool) > 0) {
                    return generatedLoot;
                }

                // 时运附魔：提高掉落概率
                var fortuneHolder = enchantmentRegistry.getHolder(Enchantments.FORTUNE);
                if (fortuneHolder.isPresent()) {
                    fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(fortuneHolder.get(), tool);
                }
            }
        }

        // 最终掉落概率：基础概率 × (1 + 时运等级 × 0.5)
        float finalChance = this.chance * (1.0f + fortuneLevel * 0.5f);

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