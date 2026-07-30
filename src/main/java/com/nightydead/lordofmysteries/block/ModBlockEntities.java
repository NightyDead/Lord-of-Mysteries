package com.nightydead.lordofmysteries.block;

import com.nightydead.lordofmysteries.LordofMysteries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 模组方块实体注册类
 * 使用 DeferredRegister 系统注册方块实体类型
 */
public class ModBlockEntities {

    /** 方块实体类型延迟注册表 */
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LordofMysteries.MODID);

    /** 炼药锅方块实体类型 */
    public static final Supplier<BlockEntityType<AlchemyCauldronBlockEntity>> ALCHEMY_CAULDRON =
            BLOCK_ENTITY_TYPES.register("alchemy_cauldron", () ->
                    BlockEntityType.Builder.of(AlchemyCauldronBlockEntity::new,
                            ModBlocks.ALCHEMY_CAULDRON.get()).build(null));

    /** 仪式祭坛方块实体类型 */
    public static final Supplier<BlockEntityType<RitualAltarBlockEntity>> RITUAL_ALTAR =
            BLOCK_ENTITY_TYPES.register("ritual_altar", () ->
                    BlockEntityType.Builder.of(RitualAltarBlockEntity::new,
                            ModBlocks.RITUAL_ALTAR.get()).build(null));

    /**
     * 将方块实体注册表绑定到模组事件总线
     *
     * @param eventBus 模组事件总线
     */
    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
