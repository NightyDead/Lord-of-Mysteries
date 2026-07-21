package com.nightydead.lordofmysteries.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.nightydead.lordofmysteries.network.C2SDivinationPacket;
import com.nightydead.lordofmysteries.skills.ModSkills;
import com.nightydead.lordofmysteries.skills.ModSkills.SkillEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
            SkillEntry skill = skills.get(selectedSector);
            if (skill.id().equals(ModSkills.ID_DIVINATION)) {
                // 群系占卜：setScreen 会自动清理轮盘，无需走 onClose
                if (isHoldingCompassAndPlant()) {
                    Minecraft.getInstance().setScreen(new BiomeDivinationScreen());
                    return;
                }
                // 结构占卜：指南针 + 岩石类方块
                if (isHoldingCompassAndStone()) {
                    Minecraft.getInstance().setScreen(new StructureDivinationScreen());
                    return;
                }
                // 矿物占卜：发送网络包后正常关闭轮盘
                PacketDistributor.sendToServer(new C2SDivinationPacket());
            }
        }
        onClose();
    }

    /**
     * 检查玩家是否一手持指南针、另一手持植物类物品
     */
    private static boolean isHoldingCompassAndPlant() {
        var player = Minecraft.getInstance().player;
        if (player == null) return false;

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        boolean mainCompass = main.is(Items.COMPASS);
        boolean offCompass = off.is(Items.COMPASS);
        boolean mainPlant = isPlantItem(main);
        boolean offPlant = isPlantItem(off);

        return (mainCompass && offPlant) || (offCompass && mainPlant);
    }

    /**
     * 判断物品是否为植物类（花、树叶、树苗、种子、草等）
     */
    private static boolean isPlantItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        // 标签匹配：花、树叶、树苗
        if (stack.is(ItemTags.FLOWERS)) return true;
        if (stack.is(ItemTags.LEAVES)) return true;
        if (stack.is(ItemTags.SAPLINGS)) return true;
        // 常见植物物品
        return stack.is(Items.SHORT_GRASS) || stack.is(Items.TALL_GRASS)
                || stack.is(Items.FERN) || stack.is(Items.LARGE_FERN)
                || stack.is(Items.VINE) || stack.is(Items.WHEAT_SEEDS)
                || stack.is(Items.MELON_SEEDS) || stack.is(Items.PUMPKIN_SEEDS)
                || stack.is(Items.BEETROOT_SEEDS) || stack.is(Items.SUGAR_CANE)
                || stack.is(Items.BAMBOO) || stack.is(Items.CACTUS)
                || stack.is(Items.SEA_PICKLE) || stack.is(Items.LILY_PAD)
                || stack.is(Items.DEAD_BUSH) || stack.is(Items.WEEPING_VINES)
                || stack.is(Items.TWISTING_VINES) || stack.is(Items.CRIMSON_FUNGUS)
                || stack.is(Items.WARPED_FUNGUS) || stack.is(Items.BROWN_MUSHROOM)
                || stack.is(Items.RED_MUSHROOM) || stack.is(Items.NETHER_SPROUTS)
                || stack.is(Items.NETHER_WART) || stack.is(Items.CHORUS_FLOWER)
                || stack.is(Items.COCOA_BEANS) || stack.is(Items.KELP)
                || stack.is(Items.SEAGRASS)
                || stack.is(Items.GLOW_LICHEN) || stack.is(Items.MOSS_BLOCK)
                || stack.is(Items.MOSS_CARPET) || stack.is(Items.HANGING_ROOTS)
                || stack.is(Items.SPORE_BLOSSOM) || stack.is(Items.AZALEA)
                || stack.is(Items.FLOWERING_AZALEA) || stack.is(Items.BIG_DRIPLEAF)
                || stack.is(Items.SMALL_DRIPLEAF) || stack.is(Items.GLOW_BERRIES)
                || stack.is(Items.SWEET_BERRIES) || stack.is(Items.PITCHER_PLANT)
                || stack.is(Items.TORCHFLOWER);
    }

    /**
     * 检查玩家是否一手持指南针、另一手持岩石类方块
     */
    private static boolean isHoldingCompassAndStone() {
        var player = Minecraft.getInstance().player;
        if (player == null) return false;

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        boolean mainCompass = main.is(Items.COMPASS);
        boolean offCompass = off.is(Items.COMPASS);
        boolean mainStone = isStoneItem(main);
        boolean offStone = isStoneItem(off);

        return (mainCompass && offStone) || (offCompass && mainStone);
    }

    /**
     * 判断物品是否为岩石类（圆石、花岗岩、石砖、黑石、下界岩等）
     */
    private static boolean isStoneItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        // 标签匹配
        if (stack.is(ItemTags.STONE_CRAFTING_MATERIALS)) return true;
        if (stack.is(ItemTags.STONE_BRICKS)) return true;
        // 圆石、石头及其变种
        if (stack.is(Items.COBBLESTONE) || stack.is(Items.STONE)
                || stack.is(Items.COBBLESTONE_SLAB) || stack.is(Items.STONE_SLAB)
                || stack.is(Items.COBBLESTONE_STAIRS) || stack.is(Items.STONE_STAIRS)
                || stack.is(Items.COBBLESTONE_WALL) || stack.is(Items.MOSSY_COBBLESTONE)
                || stack.is(Items.MOSSY_COBBLESTONE_SLAB) || stack.is(Items.MOSSY_COBBLESTONE_STAIRS)
                || stack.is(Items.MOSSY_COBBLESTONE_WALL)
                || stack.is(Items.SMOOTH_STONE) || stack.is(Items.SMOOTH_STONE_SLAB)) return true;
        // 花岗岩、闪长岩、安山岩
        if (stack.is(Items.GRANITE) || stack.is(Items.DIORITE) || stack.is(Items.ANDESITE)
                || stack.is(Items.GRANITE_SLAB) || stack.is(Items.DIORITE_SLAB) || stack.is(Items.ANDESITE_SLAB)
                || stack.is(Items.GRANITE_STAIRS) || stack.is(Items.DIORITE_STAIRS) || stack.is(Items.ANDESITE_STAIRS)
                || stack.is(Items.GRANITE_WALL) || stack.is(Items.DIORITE_WALL) || stack.is(Items.ANDESITE_WALL)
                || stack.is(Items.POLISHED_GRANITE) || stack.is(Items.POLISHED_DIORITE) || stack.is(Items.POLISHED_ANDESITE)
                || stack.is(Items.POLISHED_GRANITE_SLAB) || stack.is(Items.POLISHED_DIORITE_SLAB) || stack.is(Items.POLISHED_ANDESITE_SLAB)
                || stack.is(Items.POLISHED_GRANITE_STAIRS) || stack.is(Items.POLISHED_DIORITE_STAIRS) || stack.is(Items.POLISHED_ANDESITE_STAIRS)) return true;
        // 黑石及变种
        if (stack.is(Items.BLACKSTONE) || stack.is(Items.BLACKSTONE_SLAB)
                || stack.is(Items.BLACKSTONE_STAIRS) || stack.is(Items.BLACKSTONE_WALL)
                || stack.is(Items.POLISHED_BLACKSTONE) || stack.is(Items.POLISHED_BLACKSTONE_SLAB)
                || stack.is(Items.POLISHED_BLACKSTONE_STAIRS) || stack.is(Items.POLISHED_BLACKSTONE_WALL)
                || stack.is(Items.POLISHED_BLACKSTONE_BRICKS) || stack.is(Items.POLISHED_BLACKSTONE_BRICK_SLAB)
                || stack.is(Items.POLISHED_BLACKSTONE_BRICK_STAIRS) || stack.is(Items.POLISHED_BLACKSTONE_BRICK_WALL)
                || stack.is(Items.CHISELED_POLISHED_BLACKSTONE) || stack.is(Items.CRACKED_POLISHED_BLACKSTONE_BRICKS)
                || stack.is(Items.GILDED_BLACKSTONE)) return true;
        // 下界岩、下界砖
        if (stack.is(Items.NETHERRACK) || stack.is(Items.NETHER_BRICKS)
                || stack.is(Items.NETHER_BRICK_SLAB) || stack.is(Items.NETHER_BRICK_STAIRS)
                || stack.is(Items.NETHER_BRICK_WALL) || stack.is(Items.NETHER_BRICK_FENCE)
                || stack.is(Items.RED_NETHER_BRICKS) || stack.is(Items.RED_NETHER_BRICK_SLAB)
                || stack.is(Items.RED_NETHER_BRICK_STAIRS) || stack.is(Items.RED_NETHER_BRICK_WALL)
                || stack.is(Items.CHISELED_NETHER_BRICKS) || stack.is(Items.CRACKED_NETHER_BRICKS)
                || stack.is(Items.CRIMSON_NYLIUM) || stack.is(Items.WARPED_NYLIUM)) return true;
        // 砂岩
        if (stack.is(Items.SANDSTONE) || stack.is(Items.SANDSTONE_SLAB)
                || stack.is(Items.SANDSTONE_STAIRS) || stack.is(Items.SANDSTONE_WALL)
                || stack.is(Items.SMOOTH_SANDSTONE) || stack.is(Items.SMOOTH_SANDSTONE_SLAB)
                || stack.is(Items.SMOOTH_SANDSTONE_STAIRS) || stack.is(Items.CHISELED_SANDSTONE)
                || stack.is(Items.CUT_SANDSTONE)
                || stack.is(Items.RED_SANDSTONE) || stack.is(Items.RED_SANDSTONE_SLAB)
                || stack.is(Items.RED_SANDSTONE_STAIRS) || stack.is(Items.RED_SANDSTONE_WALL)
                || stack.is(Items.SMOOTH_RED_SANDSTONE) || stack.is(Items.SMOOTH_RED_SANDSTONE_SLAB)
                || stack.is(Items.SMOOTH_RED_SANDSTONE_STAIRS) || stack.is(Items.CHISELED_RED_SANDSTONE)) return true;
        // 深板岩
        if (stack.is(Items.DEEPSLATE) || stack.is(Items.COBBLED_DEEPSLATE)
                || stack.is(Items.COBBLED_DEEPSLATE_SLAB) || stack.is(Items.COBBLED_DEEPSLATE_STAIRS)
                || stack.is(Items.COBBLED_DEEPSLATE_WALL) || stack.is(Items.POLISHED_DEEPSLATE)
                || stack.is(Items.POLISHED_DEEPSLATE_SLAB) || stack.is(Items.POLISHED_DEEPSLATE_STAIRS)
                || stack.is(Items.POLISHED_DEEPSLATE_WALL) || stack.is(Items.DEEPSLATE_BRICKS)
                || stack.is(Items.DEEPSLATE_BRICK_SLAB) || stack.is(Items.DEEPSLATE_BRICK_STAIRS)
                || stack.is(Items.DEEPSLATE_BRICK_WALL) || stack.is(Items.DEEPSLATE_TILES)
                || stack.is(Items.DEEPSLATE_TILE_SLAB) || stack.is(Items.DEEPSLATE_TILE_STAIRS)
                || stack.is(Items.DEEPSLATE_TILE_WALL) || stack.is(Items.CHISELED_DEEPSLATE)
                || stack.is(Items.CRACKED_DEEPSLATE_BRICKS) || stack.is(Items.CRACKED_DEEPSLATE_TILES)
                || stack.is(Items.REINFORCED_DEEPSLATE)) return true;
        // 凝灰岩
        if (stack.is(Items.TUFF) || stack.is(Items.TUFF_SLAB)
                || stack.is(Items.TUFF_STAIRS) || stack.is(Items.TUFF_WALL)
                || stack.is(Items.POLISHED_TUFF) || stack.is(Items.POLISHED_TUFF_SLAB)
                || stack.is(Items.POLISHED_TUFF_STAIRS) || stack.is(Items.POLISHED_TUFF_WALL)
                || stack.is(Items.TUFF_BRICKS) || stack.is(Items.TUFF_BRICK_SLAB)
                || stack.is(Items.TUFF_BRICK_STAIRS) || stack.is(Items.TUFF_BRICK_WALL)
                || stack.is(Items.CHISELED_TUFF) || stack.is(Items.CHISELED_TUFF_BRICKS)) return true;
        // 滴水石
        if (stack.is(Items.DRIPSTONE_BLOCK) || stack.is(Items.POINTED_DRIPSTONE)) return true;
        // 方解石、玄武岩
        if (stack.is(Items.CALCITE) || stack.is(Items.BASALT)
                || stack.is(Items.SMOOTH_BASALT) || stack.is(Items.POLISHED_BASALT)) return true;
        // 末地石
        if (stack.is(Items.END_STONE) || stack.is(Items.END_STONE_BRICKS)
                || stack.is(Items.END_STONE_BRICK_SLAB) || stack.is(Items.END_STONE_BRICK_STAIRS)
                || stack.is(Items.END_STONE_BRICK_WALL)) return true;
        // 紫珀
        if (stack.is(Items.PURPUR_BLOCK) || stack.is(Items.PURPUR_SLAB)
                || stack.is(Items.PURPUR_STAIRS) || stack.is(Items.PURPUR_PILLAR)) return true;
        // 海晶石
        if (stack.is(Items.PRISMARINE) || stack.is(Items.PRISMARINE_SLAB)
                || stack.is(Items.PRISMARINE_STAIRS) || stack.is(Items.PRISMARINE_WALL)
                || stack.is(Items.PRISMARINE_BRICKS) || stack.is(Items.PRISMARINE_BRICK_SLAB)
                || stack.is(Items.PRISMARINE_BRICK_STAIRS) || stack.is(Items.DARK_PRISMARINE)
                || stack.is(Items.DARK_PRISMARINE_SLAB) || stack.is(Items.DARK_PRISMARINE_STAIRS)) return true;
        return false;
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
