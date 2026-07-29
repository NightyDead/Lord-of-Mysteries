package com.nightydead.lordofmysteries.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.client.gui.DivinationSkillWheelScreen;
import com.nightydead.lordofmysteries.client.gui.KnowledgeScreen;
import com.nightydead.lordofmysteries.network.C2SToggleVisionPacket;
import com.nightydead.lordofmysteries.skills.ModSkills;
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

    /** 👁️ 定义"开启/关闭灵视"按键，默认绑定为键盘 V 键 */
    public static final KeyMapping TOGGLE_VISION_KEY = new KeyMapping(
            "key." + LordofMysteries.MODID + ".toggle_vision",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KEY_CATEGORY
    );
    
    /** 🔮 定义"技能轮盘"按键，默认绑定为键盘 C 键（按下即打开轮盘，松开释放技能） */
    public static final KeyMapping DIVINATION_WHEEL_KEY = new KeyMapping(
            "key." + LordofMysteries.MODID + ".skill_wheel",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            KEY_CATEGORY
    );

    /** 📜 定义"知识面板"按键，默认绑定为键盘 K 键 */
    public static final KeyMapping KNOWLEDGE_PANEL_KEY = new KeyMapping(
            "key." + LordofMysteries.MODID + ".knowledge_panel",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            KEY_CATEGORY
    );
    
    /** C 键上一帧是否按下，用于检测按下瞬间（上升沿） */
    private static boolean cWasDown = false;

    /**
     * 🚀 自动注册到 Mod 总线
     * 因为 RegisterKeyMappingsEvent 继承自 IModBusEvent，NeoForge 会自动将其分流至 Mod Event Bus
     */
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_VISION_KEY);
        event.register(DIVINATION_WHEEL_KEY);
        event.register(KNOWLEDGE_PANEL_KEY);
    }

    /**
     * 🚀 自动注册到 Game 总线
     * 因为 ClientTickEvent.Post 属于普通游戏事件，NeoForge 会自动将其分流至 Game Event Bus
     * 这样我们就不需要像以前那样套一层内部类去强行指定不同的 bus 了
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        boolean cDown = DIVINATION_WHEEL_KEY.isDown();

        // ──────── 🔮 技能轮盘：C 键按下即开 ────────
        // 上升沿检测：上一帧未按下，当前帧按下，且无其他 UI 遮挡
        if (cDown && !cWasDown && mc.level != null && mc.screen == null) {
            var skills = ModSkills.getAvailableSkills(
                    ClientDataCache.getPathway(),
                    ClientDataCache.getSequence()
            );
            mc.setScreen(new DivinationSkillWheelScreen(skills));
        }
        cWasDown = cDown;

        // ──────── 👁️ 灵视：V 键即时切换（仅在无 UI 遮挡时） ────────
        if (mc.level != null && mc.screen == null) {
            while (TOGGLE_VISION_KEY.consumeClick()) {
                PacketDistributor.sendToServer(new C2SToggleVisionPacket());
            }
        }

        // ──────── 📜 知识面板：K 键按下即开 ────────
        if (mc.level != null && mc.screen == null) {
            while (KNOWLEDGE_PANEL_KEY.consumeClick()) {
                mc.setScreen(new KnowledgeScreen());
            }
        }
    }
}