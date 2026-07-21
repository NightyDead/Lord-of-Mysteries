package com.nightydead.lordofmysteries.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.nightydead.lordofmysteries.network.C2SDivinationPacket;
import com.nightydead.lordofmysteries.skills.ModSkills;
import com.nightydead.lordofmysteries.skills.ModSkills.SkillEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * 技能轮盘 Screen —— 长按 C 键弹出的径向选择菜单
 * <p>
 * <b>扇区角度系统</b>：0° 为屏幕右侧，顺时针递增。
 * 扇区 i 覆盖 [i·2π/N, (i+1)·2π/N)，此设计使得 N=2 时天然产生水平分割线（上半=扇区1，下半=扇区0）。
 * <p>
 * <b>交互规则</b>：
 * <ul>
 *   <li>鼠标落入扇区 → 高亮，中心显示技能名</li>
 *   <li>松开 C 键或鼠标左键 → 执行选中技能并关闭</li>
 *   <li>ESC → 取消关闭，不触发技能</li>
 * </ul>
 */
public class DivinationSkillWheelScreen extends Screen {

    private static final int OUTER_RADIUS = 55;
    private static final int INNER_RADIUS = 14;
    private static final int ARC_SEGMENTS = 32;

    private final List<SkillEntry> skills;
    private int selectedSector = -1;
    private int centerX;
    private int centerY;

    public DivinationSkillWheelScreen(List<SkillEntry> skills) {
        super(Component.empty());
        this.skills = skills;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ==================== 输入 ====================

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_C) {
            executeAndClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            executeAndClose();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    // ==================== 渲染 ====================

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        centerX = width / 2;
        centerY = height / 2;
        selectedSector = detectSector(mouseX, mouseY);

        // 全屏半透明遮罩
        graphics.fill(0, 0, width, height, 0x44000000);

        if (skills.isEmpty()) {
            renderEmptyWheel(graphics);
        } else {
            renderWheel(graphics);
            renderCenterLabel(graphics);
        }
    }

    // ==================== 扇区检测 ====================

    private int detectSector(int mx, int my) {
        double dx = mx - centerX;
        double dy = my - centerY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < INNER_RADIUS || dist > OUTER_RADIUS) return -1;

        // 0°=右，顺时针。N=2 时：扇区0=下半, 扇区1=上半
        double raw = Math.atan2(dy, dx);
        double norm = (raw + 2 * Math.PI) % (2 * Math.PI);
        return (int) (norm * skills.size() / (2 * Math.PI));
    }

    // ==================== 空轮盘 ====================

    private void renderEmptyWheel(GuiGraphics g) {
        drawFilledCircle(centerX, centerY, OUTER_RADIUS, 0x88000000);
        drawCircleOutline(centerX, centerY, OUTER_RADIUS, 0xFF555555);
        drawCircleOutline(centerX, centerY, INNER_RADIUS, 0xFF444444);

        Component hint = Component.translatable("skill.lordofmysteries.wheel.mortal_hint");
        int tw = font.width(hint);
        g.drawString(font, hint, centerX - tw / 2, centerY - font.lineHeight / 2, 0xFF888888, true);
    }

    // ==================== 有技能轮盘 ====================

    private void renderWheel(GuiGraphics g) {
        int n = skills.size();

        for (int i = 0; i < n; i++) {
            double sa = i * 2 * Math.PI / n;
            double ea = (i + 1) * 2 * Math.PI / n;
            boolean active = (i == selectedSector);
            int fill = active
                    ? brighten(skills.get(i).color(), 0.30F)
                    : dim(skills.get(i).color(), 0.22F);

            drawPieSector(centerX, centerY, OUTER_RADIUS, sa, ea, fill);

            // 分界线（从内圆边缘到外缘）
            double ir = INNER_RADIUS + 1;
            drawLine(
                    centerX + Math.cos(sa) * ir, centerY + Math.sin(sa) * ir,
                    centerX + Math.cos(sa) * OUTER_RADIUS, centerY + Math.sin(sa) * OUTER_RADIUS,
                    0xAA000000);
        }

        drawCircleOutline(centerX, centerY, OUTER_RADIUS, 0xDD000000);
        drawFilledCircle(centerX, centerY, INNER_RADIUS, 0xFF1A1A2E);
        drawCircleOutline(centerX, centerY, INNER_RADIUS, 0xFF444466);

        // 技能名称
        for (int i = 0; i < n; i++) {
            double mid = (i + 0.5) * 2 * Math.PI / n;
            double lr = OUTER_RADIUS * 0.58;
            int lx = centerX + (int) (Math.cos(mid) * lr);
            int ly = centerY + (int) (Math.sin(mid) * lr);
            Component name = skills.get(i).name();
            int c = (i == selectedSector) ? 0xFFFFFFFF : 0xFFDDDDDD;
            g.drawString(font, name, lx - font.width(name) / 2, ly - font.lineHeight / 2, c, true);
        }
    }

    private void renderCenterLabel(GuiGraphics g) {
        if (selectedSector >= 0 && selectedSector < skills.size()) {
            Component name = skills.get(selectedSector).name();
            int tw = font.width(name);
            g.drawString(font, name, centerX - tw / 2, centerY - font.lineHeight / 2, 0xFFFFFFFF, true);
        }
    }

    // ==================== 执行 ====================

    private void executeAndClose() {
        if (selectedSector >= 0 && selectedSector < skills.size()) {
            execute(skills.get(selectedSector));
        }
        onClose();
    }

    private void execute(SkillEntry skill) {
        if (skill.id().equals(ModSkills.ID_DIVINATION)) {
            PacketDistributor.sendToServer(new C2SDivinationPacket());
        }
    }

    // ==================== 几何绘制工具 ====================

    private static void drawFilledCircle(double cx, double cy, double r, int color) {
        drawPieSector(cx, cy, r, 0, 2 * Math.PI, color);
    }

    private static void drawCircleOutline(double cx, double cy, double r, int color) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var t = Tesselator.getInstance();
        var b = t.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= ARC_SEGMENTS; i++) {
            double a = i * 2 * Math.PI / ARC_SEGMENTS;
            b.addVertex((float) (cx + Math.cos(a) * r), (float) (cy + Math.sin(a) * r), 0).setColor(color);
        }
        RenderSystem.enableBlend();
        var mesh = b.buildOrThrow();
        if (mesh != null) BufferUploader.drawWithShader(mesh);
        RenderSystem.disableBlend();
    }

    private static void drawPieSector(double cx, double cy, double r,
                                       double sa, double ea, int color) {
        double range = ea - sa;
        if (Math.abs(range) < 0.0001) return;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var t = Tesselator.getInstance();
        var b = t.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        b.addVertex((float) cx, (float) cy, 0).setColor(color);
        for (int i = 0; i <= ARC_SEGMENTS; i++) {
            double a = sa + range * i / ARC_SEGMENTS;
            b.addVertex((float) (cx + Math.cos(a) * r), (float) (cy + Math.sin(a) * r), 0).setColor(color);
        }
        RenderSystem.enableBlend();
        var mesh = b.buildOrThrow();
        if (mesh != null) BufferUploader.drawWithShader(mesh);
        RenderSystem.disableBlend();
    }

    private static void drawLine(double x1, double y1, double x2, double y2, int color) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var t = Tesselator.getInstance();
        var b = t.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        b.addVertex((float) x1, (float) y1, 0).setColor(color);
        b.addVertex((float) x2, (float) y2, 0).setColor(color);
        RenderSystem.enableBlend();
        var mesh = b.buildOrThrow();
        if (mesh != null) BufferUploader.drawWithShader(mesh);
        RenderSystem.disableBlend();
    }

    // ==================== 颜色工具 ====================

    private static int dim(int c, float f) {
        int a = (int) (((c >> 24) & 0xFF) * f);
        int r = (int) (((c >> 16) & 0xFF) * f);
        int g = (int) (((c >> 8) & 0xFF) * f);
        int b = (int) ((c & 0xFF) * f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int brighten(int c, float f) {
        int a = (c >> 24) & 0xFF;
        int r = Math.min(255, (int) (((c >> 16) & 0xFF) * (1 + f)));
        int g = Math.min(255, (int) (((c >> 8) & 0xFF) * (1 + f)));
        int b = Math.min(255, (int) ((c & 0xFF) * (1 + f)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
