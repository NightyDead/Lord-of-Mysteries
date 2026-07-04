package com.nightydead.lordofmysteries;

import com.nightydead.lordofmysteries.block.ModBlocks;
import com.nightydead.lordofmysteries.data.ModAttachments;
import com.nightydead.lordofmysteries.data.ModDataComponents;
import com.nightydead.lordofmysteries.entity.ModEntities;
import com.nightydead.lordofmysteries.item.ModCreativeModeTabs;
import com.nightydead.lordofmysteries.item.ModItems;
import com.nightydead.lordofmysteries.loot.ModLootModifiers;
import com.nightydead.lordofmysteries.network.ModMessages;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 诡秘之主（Lord of Mysteries）模组主类
 * 负责模组的初始化、注册各类内容（物品、方块、实体、网络包等）到 NeoForge 注册系统
 * 对应 META-INF/neoforge.mods.toml 中声明的模组 ID
 */
@Mod(LordofMysteries.MODID)
public class LordofMysteries {
    /** 模组的唯一标识符（MOD ID），所有注册内容均使用此命名空间 */
    public static final String MODID = "lordofmysteries";
    /** 模组全局日志记录器，使用 SLF4J 接口，用于输出调试和运行信息 */
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 模组构造函数 - 模组加载时最先执行的代码入口
     * NeoForge 会自动注入 IEventBus（模组事件总线）和 ModContainer（模组容器）参数
     * 在此处完成所有模组内容的注册绑定
     *
     * @param modEventBus 模组生命周期事件总线，用于注册 DeferredRegister 和监听初始化事件
     * @param modContainer 模组容器，用于注册配置等模组级别扩展
     */
    public LordofMysteries(IEventBus modEventBus, ModContainer modContainer) {
        // 注册通用初始化方法，在 FMLCommonSetupEvent 触发时执行
        modEventBus.addListener(this::commonSetup);

        // 注册模组物品（非凡特性、魔药等）
        ModItems.register(modEventBus);
        // 注册模组创造模式标签页（特性标签、魔药标签、方块标签）
        ModCreativeModeTabs.register(modEventBus);
        // 注册模组方块
        ModBlocks.register(modEventBus);
        // 注册模组数据附加组件（Attachment，用于绑定玩家数据）
        ModAttachments.register(modEventBus);
        // 注册模组数据组件（DataComponent，用于物品 NBT 数据存储）
        ModDataComponents.register(modEventBus);
        // 注册模组自定义实体（如不可破坏的物品实体）
        ModEntities.register(modEventBus);
        // 注册模组网络数据包（理智、灵性、消化度、途径同步）
        ModMessages.register(modEventBus);
        ModLootModifiers.register(modEventBus);

        // 将本类注册到 NeoForge 全局事件总线，以便响应服务端事件（如 onServerStarting）
        // 注意：仅当本类中包含 @SubscribeEvent 注解的方法时才需要此行
        NeoForge.EVENT_BUS.register(this);

        // 注册物品添加到创造模式标签页的监听器
        modEventBus.addListener(this::addCreative);

        // 注册模组的通用配置（COMMON 类型），NeoForge 会自动创建并加载配置文件
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    /**
     * 通用初始化回调 - 在 FMLCommonSetupEvent 触发时执行
     * 用于执行模组加载后的初始化逻辑（如注册配方、记录调试信息等）
     *
     * @param event 通用初始化事件
     */
    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    /**
     * 将模组物品添加到对应的创造模式标签页
     * 通过 BuildCreativeModeTabContentsEvent 事件在标签页构建时注入物品
     *
     * @param event 创造模式标签页内容构建事件
     */
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
//        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
//            event.accept(EXAMPLE_BLOCK_ITEM);
//        }

    }

    /**
     * 服务端启动事件处理器
     * 使用 @SubscribeEvent 注解让事件总线自动发现并调用此方法
     *
     * @param event 服务端启动事件，可获取 MinecraftServer 实例
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
}
