package com.nightydead.lordofmysteries.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = "lordofmysteries", value = Dist.CLIENT)
public class ModHUDOverlay {

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.TAB_LIST)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int screenHeight = graphics.guiHeight();
        Font font = mc.font;

        // 设定我们面板的左上角绝对基准坐标（往上挪一点，给身份文本留位置）
        int startX = 10;
        int startY = screenHeight - 85; // 从 -65 提到 -85

        // 从客户端大本营提取非凡身份（💡 确保你的 ClientDataCache 有这两个方法）
        String pathway = ClientDataCache.getPathway(); // 默认可以是 "none" 或 "凡人"
        int sequence = ClientDataCache.getSequence();    // 默认可以是 10

        // ==================== 👁️ 0. 神秘学身份看板渲染 ====================
        String identityText;
        int identityColor;

        if (pathway == null || pathway.equalsIgnoreCase("none") || sequence >= 10) {
            identityText = "【 身份: 普通凡人 】";
            identityColor = 0xFFAAAAAA; // 凡人是朴素的灰色
        } else {
            // 智能化途径名称本地化/美化转换
            String pathwayName = pathway;
            if (pathway.equalsIgnoreCase("fool")) pathwayName = "占卜家（愚者）";
            // 如果有其他途径可以在这里继续 else if 转换

            identityText = "【 " + pathwayName + "途径 · 序列 " + sequence + " 】";

            // 根据序列高低调整称号尊贵颜色
            if (sequence <= 4) {
                identityText = "👑 " + identityText; // 高序列半神自带皇冠
                identityColor = 0xFFFFAA00; // 尊贵金
            } else if (sequence <= 7) {
                identityColor = 0xFF55FFFF; // 中序列青色
            } else {
                identityColor = 0xFF55FF55; // 低序列绿色
            }
        }

        // 渲染身份文本，带阴影
        graphics.drawString(font, Component.literal(identityText), startX, startY, identityColor, true);

        // 让下方的条目顺次下移 15 个像素
        startY += 15;

        // ==================== 🧠 1. 理智度渲染 ====================
        int sanity = ClientDataCache.getSanity();
        String sanityText = "理智: " + sanity + " / 100";
        int sanityColor = sanity <= 30 ? 0xFFAA0000 : (sanity <= 70 ? 0xFFFFAA00 : 0xFFFFFFFF);
        graphics.drawString(font, Component.literal(sanityText), startX, startY, sanityColor, true);

        graphics.fill(startX, startY + 10, startX + 100, startY + 14, 0x55555555);
        int sanityBarWidth = Math.max(0, Math.min(100, sanity));
        graphics.fill(startX, startY + 10, startX + sanityBarWidth, startY + 14, 0xFFFF5555);

        // ==================== 🔮 2. 灵性值渲染 ====================
        int spirituality = ClientDataCache.getSpirituality();
        int maxSpirituality = ClientDataCache.getMaxSpirituality();
        String spText = "灵性: " + spirituality + " / " + maxSpirituality;
        int spColor = maxSpirituality > 0 ? 0xFFCC55FF : 0xFFAAAAAA;
        graphics.drawString(font, Component.literal(spText), startX, startY + 18, spColor, true);

        graphics.fill(startX, startY + 28, startX + 100, startY + 32, 0x55555555);
        if (maxSpirituality > 0) {
            float spPercent = (float) spirituality / maxSpirituality;
            int spBarWidth = (int) (Math.max(0.0F, Math.min(1.0F, spPercent)) * 100);
            graphics.fill(startX, startY + 28, startX + spBarWidth, startY + 32, 0xFF5555FF);
        }

        // ==================== 🌟 3. 消化度渲染 ====================
        float digestion = ClientDataCache.getDigestion();
        if (maxSpirituality > 0) {
            int digestionPercent = (int) (digestion * 100);
            String digText = "魔药消化度: " + digestionPercent + "%";
            int digColor = digestion >= 1.0F ? 0xFFFFFF55 : 0xFF55FFFF;
            graphics.drawString(font, Component.literal(digText), startX, startY + 36, digColor, true);

            graphics.fill(startX, startY + 46, startX + 100, startY + 50, 0x55555555);
            int digBarWidth = (int) (Math.max(0.0F, Math.min(1.0F, digestion)) * 100);
            int barColor = digestion >= 1.0F ? 0xFFFFFF55 : 0xFF55FFFF;
            graphics.fill(startX, startY + 46, startX + digBarWidth, startY + 50, barColor);
        }
    }
}