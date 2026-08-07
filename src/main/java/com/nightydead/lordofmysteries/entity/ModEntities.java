package com.nightydead.lordofmysteries.entity;

import com.nightydead.lordofmysteries.LordofMysteries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 模组实体注册类
 * 管理所有自定义实体类型的延迟注册与事件总线绑定
 */
public class ModEntities {

    /** 实体类型延迟注册表，使用模组 ID 作为命名空间 */
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, LordofMysteries.MODID);

    /**
     * 不可破坏的物品实体类型 (取代原版 ItemEntity 实现特性不灭)
     */
    public static final DeferredHolder<EntityType<?>, EntityType<IndestructibleItemEntity>> INDESTRUCTIBLE_ITEM =
            ENTITY_TYPES.register("indestructible_item", () -> EntityType.Builder.<IndestructibleItemEntity>of(
                            IndestructibleItemEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("indestructible_item")
            );

    /**
     * 化纸为刀的纸刀抛射物实体 (直线飞行、无重力，命中或超程即消失不可回收)
     */
    public static final DeferredHolder<EntityType<?>, EntityType<PaperKnifeEntity>> PAPER_KNIFE =
            ENTITY_TYPES.register("paper_knife", () -> EntityType.Builder.<PaperKnifeEntity>of(
                            PaperKnifeEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("paper_knife")
            );

    /**
     * 拉瓦章鱼实体（继承荧光鱿鱼，在下界岩浆湖中生成）
     * 使用 CREATURE 分类配合 IN_LAVA 生成类型
     */
    public static final DeferredHolder<EntityType<?>, EntityType<LavaOctopusEntity>> LAVA_OCTOPUS =
            ENTITY_TYPES.register("lava_octopus", () -> EntityType.Builder.<LavaOctopusEntity>of(
                            LavaOctopusEntity::new, MobCategory.CREATURE)
                    .sized(0.8F, 0.8F)
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .build("lava_octopus")
            );

    /**
     * 将实体类型注册表绑定到模组事件总线
     *
     * @param eventBus 模组事件总线
     */
    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}