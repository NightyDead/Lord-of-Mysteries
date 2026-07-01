package com.nightydead.lordofmysteries.entity;

import com.nightydead.lordofmysteries.LordofMysteries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, LordofMysteries.MODID);


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}