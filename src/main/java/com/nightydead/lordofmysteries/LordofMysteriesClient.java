package com.nightydead.lordofmysteries;

import com.nightydead.lordofmysteries.block.ModBlockEntities;
import com.nightydead.lordofmysteries.client.AlchemyCauldronRenderer;
import com.nightydead.lordofmysteries.client.RitualAltarRenderer;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * 模组客户端入口类 - 仅在客户端（Dist.CLIENT）加载，服务端不会加载此类
 * 负责处理客户端专属的初始化逻辑，如配置界面注册、客户端事件监听等
 * 使用 @EventBusSubscriber 注解自动注册类中所有带 @SubscribeEvent 的静态方法到客户端事件总线
 */
@Mod(value = LordofMysteries.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = LordofMysteries.MODID, value = Dist.CLIENT)
public class LordofMysteriesClient {

    /**
     * 客户端模组构造函数
     * 注册模组的配置界面扩展点，使玩家可以在模组列表中点击"配置"按钮进入配置界面
     *
     * @param container 模组容器，用于注册客户端扩展点
     */
    public LordofMysteriesClient(ModContainer container) {
        // 注册 NeoForge 内置的配置界面工厂
        // 玩家可以通过 Mods 界面 > 选择本模组 > 点击 Config 按钮访问配置
        // 注意：需要在 en_us.json 等语言文件中为配置项添加对应的翻译
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    /**
     * 客户端初始化回调 - 在 FMLClientSetupEvent 触发时执行
     * 用于执行仅在客户端运行的初始化逻辑（如渲染器注册、键绑定注册等）
     *
     * @param event 客户端初始化事件
     */
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        LordofMysteries.LOGGER.info("HELLO FROM CLIENT SETUP");
        LordofMysteries.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    /**
     * 注册方块实体渲染器
     * 为炼药锅注册自定义渲染器，实现锅内物品悬浮旋转效果
     *
     * @param event 渲染器注册事件
     */
    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.ALCHEMY_CAULDRON.get(), AlchemyCauldronRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RITUAL_ALTAR.get(), RitualAltarRenderer::new);
    }
}
