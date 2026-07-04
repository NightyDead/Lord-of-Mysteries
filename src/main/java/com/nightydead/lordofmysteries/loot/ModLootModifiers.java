package com.nightydead.lordofmysteries.loot;

import com.mojang.serialization.MapCodec;
import com.nightydead.lordofmysteries.LordofMysteries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.function.Supplier;

public class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, LordofMysteries.MODID);

    // 必须确保这里的键名 "entity_drop" 和 JSON 文件中的 "type": "lordofmysteries:entity_drop" 完全对齐
    public static final Supplier<MapCodec<EntityLootModifier>> ENTITY_DROP =
            LOOT_MODIFIER_SERIALIZERS.register("entity_drop", () -> EntityLootModifier.CODEC.get());

    public static void register(IEventBus eventBus) {
        LOOT_MODIFIER_SERIALIZERS.register(eventBus);
    }
}