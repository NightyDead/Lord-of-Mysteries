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

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, LordofMysteries.MODID);

    // 注册非凡途径组件（存字符串，如 "fool", "error"）
    public static final Supplier<DataComponentType<String>> PATHWAY = DATA_COMPONENT_TYPES.register(
            "pathway", () -> DataComponentType.<String>builder()
                    .persistent(ExtraCodecs.NON_EMPTY_STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build()
    );

    // 注册序列等级组件（存整数，0-9）
    public static final Supplier<DataComponentType<Integer>> SEQUENCE = DATA_COMPONENT_TYPES.register(
            "sequence", () -> DataComponentType.<Integer>builder()
                    .persistent(ExtraCodecs.POSITIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );

    // 最大灵性值组件
    public static final Supplier<DataComponentType<Integer>> MAX_SPIRITUALITY = DATA_COMPONENT_TYPES.register(
            "max_spirituality", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );

    // ✨ 工业级优化：为特性聚合列表添加安全传输上限限制，彻底杜绝恶意大数据包或超长历史导致客户端断开连接
    public static final Supplier<DataComponentType<List<String>>> AGGREGATED_FEATURES = DATA_COMPONENT_TYPES.register(
            "aggregated_features", () -> DataComponentType.<List<String>>builder()
                    .persistent(ExtraCodecs.nonEmptyList(ExtraCodecs.NON_EMPTY_STRING.listOf())) // 存档编码
                    // 📡 1.21.1 推荐的安全写法：显式指定列表的最大反序列化长度限制（例如最大允许 256 条特性记录）
                    // 这样写不仅语义极度清晰，还能完美享受原版底层对底层 ByteBuf 的边界防过载保护
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(256)))
                    .build()
    );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}