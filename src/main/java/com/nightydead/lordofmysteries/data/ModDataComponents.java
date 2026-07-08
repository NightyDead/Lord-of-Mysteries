package com.nightydead.lordofmysteries.data;

import com.mojang.serialization.Codec;
import com.nightydead.lordofmysteries.LordofMysteries;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

/**
 * 模组数据组件（DataComponent）注册类
 * 挂载自定义神秘学数据至 ItemStack，支持 1.21.1 自动持久化与网络同步
 */
public class ModDataComponents {

    /** 数据组件类型延迟注册表，使用模组 ID 作为命名空间 */
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, LordofMysteries.MODID);

    /** 非凡途径组件 - 存储对应的途径 ID（如 "fool"） */
    public static final Supplier<DataComponentType<String>> PATHWAY = DATA_COMPONENT_TYPES.register(
            "pathway", () -> DataComponentType.<String>builder()
                    .persistent(ExtraCodecs.NON_EMPTY_STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build()
    );

    /** 序列等级组件 - 存储序列号（0-10） */
    public static final Supplier<DataComponentType<Integer>> SEQUENCE = DATA_COMPONENT_TYPES.register(
            "sequence", () -> DataComponentType.<Integer>builder()
                    .persistent(ExtraCodecs.POSITIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );

    /** 最大灵性值组件 - 存储吸收后赋予的灵性上限 */
    public static final Supplier<DataComponentType<Integer>> MAX_SPIRITUALITY = DATA_COMPONENT_TYPES.register(
            "max_spirituality", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );

    /** 聚合特性列表组件 - 存储死亡析出的全部特性历史记录（格式 "pathway:sequence"） */
    public static final Supplier<DataComponentType<List<String>>> AGGREGATED_FEATURES = DATA_COMPONENT_TYPES.register(
            "aggregated_features", () -> DataComponentType.<List<String>>builder()
                    .persistent(ExtraCodecs.nonEmptyList(ExtraCodecs.NON_EMPTY_STRING.listOf()))
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(256))) // 防极端封包注入
                    .build()
    );

    /**
     * 将数据组件注册表绑定到模组事件总线
     *
     * @param eventBus 模组事件总线
     */
    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}