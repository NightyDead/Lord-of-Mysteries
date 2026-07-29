package com.nightydead.lordofmysteries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 模组配置类 - 管理模组的所有可配置选项
 * 使用 NeoForge 的 ModConfigSpec 系统，支持通过配置文件修改模组行为
 * 配置项会在模组加载时自动注册，并可通过游戏内的配置界面或外部配置文件进行修改
 */
public class Config {

    /** 知识死亡惩罚模式 */
    public enum DeathPenaltyMode {
        /** 知识不丢 */
        NONE,
        /** 记忆模糊debuff */
        MEMORY_FOG,
        /** 丢失部分知识 */
        PARTIAL_LOSS
    }

    /** 配置构建器，用于逐步定义所有配置项，最终调用 build() 生成不可变的配置规范 */
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** 知识死亡惩罚模式配置 */
    public static final ModConfigSpec.EnumValue<DeathPenaltyMode> DEATH_PENALTY = BUILDER
            .comment("Death penalty for learned potion recipes: NONE (keep all), MEMORY_FOG (temp debuff), PARTIAL_LOSS (lose some)")
            .defineEnum("deathPenalty", DeathPenaltyMode.NONE);

    /** 是否在游戏初始化时记录泥土方块信息的开关（调试用） */
    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    /** 一个示例魔法数字配置项，取值范围为 0 到 Integer.MAX_VALUE，默认值为 42 */
    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    /** 魔法数字的介绍前缀文本，可在配置文件中自定义 */
    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    /**
     * 物品名称列表配置项
     * 存储一组被视为资源位置（ResourceLocation）的物品 ID 字符串
     * 在模组初始化时会逐一记录这些物品，用于调试或自定义内容过滤
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);

    /** 构建最终的配置规范对象，供 NeoForge 注册和使用 */
    static final ModConfigSpec SPEC = BUILDER.build();

    /**
     * 验证物品名称是否为有效的资源位置且存在于游戏注册表中
     * @param obj 待验证的对象，应为字符串类型的物品 ID
     * @return 如果该物品 ID 合法且存在于原版物品注册表中则返回 true
     */
    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
