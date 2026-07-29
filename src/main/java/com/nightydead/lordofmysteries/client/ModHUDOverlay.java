package com.nightydead.lordofmysteries.client;

import com.nightydead.lordofmysteries.LordofMysteries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * 模组 HUD 覆盖层渲染器
 * 负责在游戏客户端界面的左上角优雅绘制非凡者的神秘学核心状态面板
 */
@EventBusSubscriber(modid = LordofMysteries.MODID, value = Dist.CLIENT)
public class ModHUDOverlay {

    /**
     * HUD 覆盖层渲染回调
     * 在原版 TAB_LIST 层渲染后叠加神秘学状态面板
     * 包括身份看板、理智度、灵性值、魔药消化度等核心状态显示
     *
     * @param event GUI 层渲染事件
     */
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiLayerEvent.Post event) {
        // 仅在原版 TAB_LIST 层渲染后叠加，确保不会因虚空图层触发重复重绘
        if (!event.getName().equals(VanillaGuiLayers.TAB_LIST)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        // 当 F3 调试界面打开时隐藏模组 HUD，避免遮挡调试信息
        if (mc.getDebugOverlay().showDebugScreen()) return;

        GuiGraphics graphics = event.getGuiGraphics();
        Font font = mc.font;

        // 获取当前游戏屏幕的实际动态宽高
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        // 设定神秘学面板的左上角绝对基准坐标（左上角）
        int startX = 10;
        int currentY = 10;

        // ==================== 👁️ 灵视全屏幽蓝滤镜特效 ====================
        // 从客户端本地数据缓存中提取灵视激活状态
        if (ClientDataCache.isSpiritVisionActive()) {
            // 使用 fill 绘制一个覆盖全屏幕的半透明灵界幽蓝色（2F 前两位为十六进制透明度，后六位为幽蓝色）
            graphics.fill(0, 0, width, height, 0x2F003366);

            // 在面板顶部显示灵视激活状态
            graphics.drawString(font, Component.literal("👁 灵视状态已激活 (Spirit Vision)"), startX, currentY, 0x55FFFFFF, true);
            currentY += 15;
        }

        // 提取当前的非凡身份缓存数据
        String pathway = ClientDataCache.getPathway();
        int sequence = ClientDataCache.getSequence();

        // ==================== 👁️ 0. 神秘学身份看板 ====================
        Component identityText;
        int identityColor;

        boolean isMortal = pathway == null || pathway.equalsIgnoreCase("none") || sequence >= 10; //

        if (isMortal) {
            identityText = Component.translatable("hud.lordofmysteries.identity.mortal"); //
            identityColor = 0xFFAAAAAA; // 凡人为朴素灰色
        } else {
            // 通过统一的语言包 key 动态获取途径名称，完全解耦硬编码
            String translationKey = "pathway." + LordofMysteries.MODID + "." + pathway.toLowerCase(); //
            Component pathwayName = Component.translatable(translationKey); //
            identityText = Component.translatable("hud.lordofmysteries.identity.format", pathwayName, sequence); //

            // 根据序列层级赋予尊贵的颜色变化
            if (sequence <= 4) {
                identityText = Component.literal("👑 ").append(identityText); // 半神及以上冠以神圣冠冕
                identityColor = 0xFFFFAA00; // 尊贵金
            } else if (sequence <= 7) {
                identityColor = 0xFF55FFFF; // 中序列青色
            } else {
                identityColor = 0xFF55FF55; // 低序列绿色
            }
        }

        // 如果开启了灵视，在身份牌上方显示灵视运行标记
        if (ClientDataCache.isSpiritVisionActive() && !isMortal) {
            graphics.drawString(font, Component.literal("§b⚡ 灵视运行中"), startX, currentY, 0xFF55FFFF, true);
            currentY += 12;
        }

        graphics.drawString(font, identityText, startX, currentY, identityColor, true); //
        currentY += 15; // 步进安全下移

        // ==================== 🧠 1. 理智度 (Sanity) ====================
        int sanity = ClientDataCache.getSanity(); //
        int sanityColor = sanity <= 30 ? 0xFFAA0000 : (sanity <= 70 ? 0xFFFFAA00 : 0xFFFFFFFF); //

        graphics.drawString(font, Component.translatable("hud.lordofmysteries.sanity.text", sanity), startX, currentY, sanityColor, true); //

        // 绘制理智血条
        graphics.fill(startX, currentY + 10, startX + 100, currentY + 14, 0x55555555); //
        int sanityBarWidth = Math.clamp(sanity, 0, 100); //
        graphics.fill(startX, currentY + 10, startX + sanityBarWidth, currentY + 14, 0xFFFF5555); //
        currentY += 18; //

        // ==================== 🔮 2. 灵性值 与 魔药消化度 ====================
        int spirituality = ClientDataCache.getSpirituality(); //
        int maxSpirituality = ClientDataCache.getMaxSpirituality(); //

        // 🔮 只有当拥有最大灵性上限时（即成为了非凡者），才渲染接下来的超凡专属属性
        if (maxSpirituality > 0 && !isMortal) {
            graphics.drawString(font, Component.translatable("hud.lordofmysteries.spirituality.text", spirituality, maxSpirituality), startX, currentY, 0xFFCC55FF, true); //

            // 绘制灵性条
            graphics.fill(startX, currentY + 10, startX + 100, currentY + 14, 0x55555555); //
            float spPercent = (float) spirituality / maxSpirituality; //
            int spBarWidth = (int) (Math.clamp(spPercent, 0.0F, 1.0F) * 100); //
            graphics.fill(startX, currentY + 10, startX + spBarWidth, currentY + 14, 0xFF5555FF); //
            currentY += 18; //

            // ==================== 🌟 3. 魔药消化度 (仅非凡者可见) ====================
            int digestion = ClientDataCache.getDigestion(); //
            int maxDigestion = ClientDataCache.getMaxDigestion(); //
            int digColor = digestion >= maxDigestion ? 0xFFFFFF55 : 0xFF55FFFF; // 完全消化为金色，否则青色

            // 显示为 "魔药消化度: X / Y" 格式
            graphics.drawString(font, Component.translatable("hud.lordofmysteries.digestion.text", digestion, maxDigestion), startX, currentY, digColor, true); //

            // 绘制消化条
            graphics.fill(startX, currentY + 10, startX + 100, currentY + 14, 0x55555555); //
            float digPercent = maxDigestion > 0 ? Math.clamp((float) digestion / maxDigestion, 0.0F, 1.0F) : 0.0F;
            int digBarWidth = (int) (digPercent * 100); //
            graphics.fill(startX, currentY + 10, startX + digBarWidth, currentY + 14, digColor); //
        }
    }
}