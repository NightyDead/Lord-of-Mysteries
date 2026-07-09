package com.nightydead.lordofmysteries.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.network.C2SToggleVisionPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * 模组客户端按键绑定与监听总控类
 * 完美适配 NeoForge 1.21.1+ 规范，不再指定显式 bus，由系统根据事件类型自动分流注册
 */
@EventBusSubscriber(modid = LordofMysteries.MODID, value = Dist.CLIENT)
public class ModKeyMappings {

    /** 统一定义按键分类在控制菜单中的本地化语言键 (Lang Key) */
    private static final String KEY_CATEGORY = "key.categories." + LordofMysteries.MODID;

    /** 👁️ 定义“开启/关闭灵视”按键，默认绑定为键盘 V 键 */
    public static final KeyMapping TOGGLE_VISION_KEY = new KeyMapping(
            "key." + LordofMysteries.MODID + ".toggle_vision", // 按键本身的本地化键
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,                                   // 默认按键为 V
            KEY_CATEGORY                                      // 所属分类
    );

    /**
     * 🚀 自动注册到 Mod 总线
     * 因为 RegisterKeyMappingsEvent 继承自 IModBusEvent，NeoForge 会自动将其分流至 Mod Event Bus
     */
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_VISION_KEY);
    }

    /**
     * 🚀 自动注册到 Game 总线
     * 因为 ClientTickEvent.Post 属于普通游戏事件，NeoForge 会自动将其分流至 Game Event Bus
     * 这样我们就不需要像以前那样套一层内部类去强行指定不同的 bus 了
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        // 确保只在游戏世界内且玩家未打开任何 UI 界面（如背包、聊天栏）时才响应快捷键
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().screen == null) {

            // ⚡ 检查“灵视按键”是否在当前 Tick 被按下
            while (TOGGLE_VISION_KEY.consumeClick()) {
                // 🚀 向服务器提交“我想开关灵视”的请求
                PacketDistributor.sendToServer(new C2SToggleVisionPacket());
            }
        }
    }
}