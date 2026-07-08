package com.nightydead.lordofmysteries.loot;

import com.mojang.serialization.MapCodec;
import com.nightydead.lordofmysteries.LordofMysteries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.function.Supplier;

/**
 * 全局战利品修饰器注册类
 * 负责注册模组自定义的战利品修饰器序列化器
 * 包括实体掉落修饰器和方块掉落修饰器
 */
public class ModLootModifiers {

    /** 战利品修饰器序列化器延迟注册表 */
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, LordofMysteries.MODID);

    /** 实体掉落修饰器 - 支持抢夺附魔加成的概率掉落 */
    public static final Supplier<MapCodec<EntityLootModifier>> ENTITY_DROP =
            LOOT_MODIFIER_SERIALIZERS.register("entity_drop", () -> EntityLootModifier.CODEC.get());

    /** 方块掉落修饰器 - 支持时运附魔加成的概率掉落 */
    public static final Supplier<MapCodec<BlockLootModifier>> BLOCK_DROP =
            LOOT_MODIFIER_SERIALIZERS.register("block_drop", () -> BlockLootModifier.CODEC.get());

    /**
     * 将战利品修饰器注册表绑定到模组事件总线
     *
     * @param eventBus 模组事件总线
     */
    public static void register(IEventBus eventBus) {
        LOOT_MODIFIER_SERIALIZERS.register(eventBus);
    }
}