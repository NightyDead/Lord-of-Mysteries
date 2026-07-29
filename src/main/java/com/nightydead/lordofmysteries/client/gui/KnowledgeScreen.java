package com.nightydead.lordofmysteries.client.gui;

import com.nightydead.lordofmysteries.LordofMysteries;
import com.nightydead.lordofmysteries.client.ClientDataCache;
import com.nightydead.lordofmysteries.data.PotionRecipeData;
import com.nightydead.lordofmysteries.data.PotionRecipeRegistry;
import com.nightydead.lordofmysteries.skills.ModSkills;
import com.nightydead.lordofmysteries.skills.ModSkills.SkillEntry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.*;

/**
 * 知识面板 —— 按 K 键打开的超凡知识总览界面
 * <p>
 * 凡人（无途径）且有已学配方 → 显示已学配方及其材料/获取方式<br>
 * 凡人且无已学配方 → 显示"尚未触及超凡的门槛"<br>
 * 非凡者 → 显示当前身份、技能、已学配方<br>
 * 所有数据从客户端缓存读取，关闭时不发网络包
 */
public class KnowledgeScreen extends Screen {

    private static final int CONTENT_LEFT = 30;
    private static final int LINE_HEIGHT = 14;
    private static final int SECTION_GAP = 10;
    /** 内容区距窗口右边缘的保留空间（滚动条宽6 + 两侧各4缓冲） */
    private static final int CONTENT_RIGHT_PAD = 14;

    private int scrollOffset = 0;
    private int maxScroll = 0;
    private boolean isScrolling = false;

    public KnowledgeScreen() {
        super(Component.translatable("screen.lordofmysteries.knowledge"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);

        Font font = this.font;
        int startY = 30 - scrollOffset;
        int contentBottom = this.height - 20;
        // 内容区右边界（为滚动条留空间）
        int contentMaxX = this.width - CONTENT_RIGHT_PAD;

        // 绘制标题
        graphics.drawCenteredString(font, this.title, this.width / 2, 10, 0xFFCCAA44);

        String pathway = ClientDataCache.getPathway();
        int sequence = ClientDataCache.getSequence();
        boolean isMortal = pathway == null || pathway.equalsIgnoreCase("none") || sequence >= 10;

        // ==================== 非凡人：身份区 ====================
        Set<String> ownRecipes = new HashSet<>();
        if (!isMortal) {
            drawSectionTitle(graphics, font, startY,
                    Component.translatable("screen.lordofmysteries.knowledge.current_identity"), 0xFFAA66FF);
            startY += LINE_HEIGHT + 2;

            String pathKey = "pathway." + LordofMysteries.MODID + "." + pathway.toLowerCase();
            String seqKey = "sequence." + LordofMysteries.MODID + "." + pathway.toLowerCase() + "." + sequence;
            Component identityText = Component.translatable("hud.lordofmysteries.identity.format",
                    Component.translatable(pathKey), sequence);
            graphics.drawString(font, "  " + identityText.getString(), CONTENT_LEFT + 8, startY, 0xFFFFDD88);
            startY += LINE_HEIGHT + 4;

            Component nameText = Component.translatable(seqKey);
            graphics.drawString(font, "  " + Component.translatable("tooltip.lordofmysteries.recipe.sequence",
                    sequence, nameText).getString(), CONTENT_LEFT + 8, startY, 0xFFDDAAAA);
            startY += LINE_HEIGHT + SECTION_GAP;

            // 技能区
            List<SkillEntry> skills = ModSkills.getAvailableSkills(pathway, sequence);
            if (!skills.isEmpty()) {
                drawSectionTitle(graphics, font, startY,
                        Component.translatable("screen.lordofmysteries.knowledge.skills"), 0xFF66AAFF);
                startY += LINE_HEIGHT + 2;
                for (SkillEntry skill : skills) {
                    graphics.drawString(font, "  ◆ " + skill.name().getString(), CONTENT_LEFT + 8, startY, 0xFFCCDDFF);
                    startY += LINE_HEIGHT;
                }
                startY += SECTION_GAP;
            }

            // 配方卡片内文本从 CONTENT_LEFT + 8 开始
            int cardTextX = CONTENT_LEFT + 8;
            int cardMaxWidth = Math.max(80, contentMaxX - cardTextX);

            // 显示自己途径当前序列和上一序列的魔药配方
            PotionRecipeData currentRecipe = PotionRecipeRegistry.get(pathway, sequence);
            if (currentRecipe != null) {
                ownRecipes.add(pathway + ":" + sequence);
                startY = drawRecipeCard(graphics, font, startY, currentRecipe, sequence, cardMaxWidth) + SECTION_GAP;
            }
            if (sequence < 9) {
                PotionRecipeData prevRecipe = PotionRecipeRegistry.get(pathway, sequence + 1);
                if (prevRecipe != null) {
                    ownRecipes.add(pathway + ":" + (sequence + 1));
                    startY = drawRecipeCard(graphics, font, startY, prevRecipe, sequence + 1, cardMaxWidth) + SECTION_GAP;
                }
            }
        }

        // ==================== 已学配方区 ====================
        Set<String> learned = ClientDataCache.getLearnedRecipes();
        Set<String> displayLearned = new HashSet<>(learned);
        displayLearned.removeAll(ownRecipes);

        if (!displayLearned.isEmpty()) {
            drawSectionTitle(graphics, font, startY,
                    Component.translatable("screen.lordofmysteries.knowledge.learned_recipes"), 0xFF55AA55);
            startY += LINE_HEIGHT + 2;

            // 已学配方区文本从 CONTENT_LEFT + 16 开始
            int learnedTextX = CONTENT_LEFT + 16;
            int learnedMaxWidth = Math.max(80, contentMaxX - learnedTextX);

            Map<String, List<Integer>> grouped = groupLearnedByPathway(displayLearned);
            for (var entry : grouped.entrySet()) {
                String pw = entry.getKey();
                String pwKey = "pathway." + LordofMysteries.MODID + "." + pw;
                graphics.drawString(font, "  §l" + Component.translatable(pwKey).getString(),
                        CONTENT_LEFT + 8, startY, 0xFFFFFF88);
                startY += LINE_HEIGHT;

                List<Integer> seqs = entry.getValue();
                seqs.sort(Comparator.naturalOrder());
                for (int seq : seqs) {
                    String sKey = "sequence." + LordofMysteries.MODID + "." + pw + "." + seq;
                    String sName = Component.translatable(sKey).getString();
                    graphics.drawString(font, "    · 序列" + seq + " " + sName,
                            CONTENT_LEFT + 12, startY, 0xFFCCCCCC);
                    startY += LINE_HEIGHT;

                    PotionRecipeData recipeData = PotionRecipeRegistry.get(pw, seq);
                    if (recipeData != null) {
                        if (recipeData.mainMaterials() != null && !recipeData.mainMaterials().isEmpty()) {
                            graphics.drawString(font, "      主材: " + translateMaterials(recipeData.mainMaterials()),
                                    CONTENT_LEFT + 16, startY, 0xFFAAAAAA);
                            startY += LINE_HEIGHT;
                        }
                        if (recipeData.auxiliaryMaterials() != null && !recipeData.auxiliaryMaterials().isEmpty()) {
                            graphics.drawString(font, "      辅材: " + translateMaterials(recipeData.auxiliaryMaterials()),
                                    CONTENT_LEFT + 16, startY, 0xFFAAAAAA);
                            startY += LINE_HEIGHT;
                        }
                        if (recipeData.acquisition() != null && !recipeData.acquisition().isEmpty()) {
                            startY += drawWrappedText(graphics, font,
                                    "      获取: " + Component.translatable(recipeData.acquisition()).getString(),
                                    CONTENT_LEFT + 16, startY, 0xFF999999,
                                    learnedMaxWidth);
                        }
                    }
                }
                startY += 4;
            }
        }

        // ==================== 凡人且无配方：空白提示 ====================
        if (isMortal && learned.isEmpty()) {
            drawSectionTitle(graphics, font, startY,
                    Component.translatable("screen.lordofmysteries.knowledge.mortal"), 0xFF888888);
            startY += LINE_HEIGHT;
        }

        // 计算最大滚动距离
        maxScroll = Math.max(0, startY - contentBottom + scrollOffset);

        // 绘制滚动条
        if (maxScroll > 0) {
            int scrollBarX = this.width - 10;
            int totalContent = Math.max(startY + scrollOffset - 30, 1);
            int scrollBarHeight = Math.max(20, (int) ((float) (contentBottom - 30) / totalContent * (contentBottom - 30)));
            int scrollBarY = 30 + (int) ((float) scrollOffset / maxScroll * (contentBottom - 30 - scrollBarHeight));
            graphics.fill(scrollBarX, 30, scrollBarX + 6, contentBottom, 0x44FFFFFF);
            graphics.fill(scrollBarX, scrollBarY, scrollBarX + 6, scrollBarY + scrollBarHeight, 0xFFAAAAAA);
        }
    }

    /** 绘制分区标题 */
    private void drawSectionTitle(GuiGraphics graphics, Font font, int y, Component text, int color) {
        graphics.drawString(font, text, CONTENT_LEFT, y, color);
    }

    /** 绘制配方卡片（含主材、辅材、获取方式），材料名使用翻译，长文本自动换行。返回绘制后的 y 坐标 */
    private int drawRecipeCard(GuiGraphics graphics, Font font, int y, PotionRecipeData data, int seq, int maxTextWidth) {
        String seqKey = "sequence." + LordofMysteries.MODID + "." + data.pathway() + "." + seq;
        String titleStr = "▸ " + Component.translatable(seqKey).getString() + " (序列" + seq + ")";
        graphics.drawString(font, titleStr, CONTENT_LEFT + 4, y, 0xFFDDCC88);
        y += LINE_HEIGHT;

        if (data.mainMaterials() != null && !data.mainMaterials().isEmpty()) {
            graphics.drawString(font,
                    "  " + Component.translatable("screen.lordofmysteries.knowledge.main_materials").getString()
                            + " " + translateMaterials(data.mainMaterials()),
                    CONTENT_LEFT + 8, y, 0xFFAAAAAA);
            y += LINE_HEIGHT;
        }
        if (data.auxiliaryMaterials() != null && !data.auxiliaryMaterials().isEmpty()) {
            graphics.drawString(font,
                    "  " + Component.translatable("screen.lordofmysteries.knowledge.aux_materials").getString()
                            + " " + translateMaterials(data.auxiliaryMaterials()),
                    CONTENT_LEFT + 8, y, 0xFFAAAAAA);
            y += LINE_HEIGHT;
        }
        if (data.acquisition() != null && !data.acquisition().isEmpty()) {
            String prefix = "  " + Component.translatable("screen.lordofmysteries.knowledge.acquisition").getString() + " ";
            y += drawWrappedText(graphics, font, prefix + Component.translatable(data.acquisition()).getString(),
                    CONTENT_LEFT + 8, y, 0xFFAAAAAA, maxTextWidth);
        }
        return y;
    }

    /** 绘制带自动换行的文本，返回占用的总行高 */
    private int drawWrappedText(GuiGraphics graphics, Font font, String text, int x, int y, int color, int maxWidth) {
        var lines = font.split(Component.literal(text), maxWidth);
        for (var line : lines) {
            graphics.drawString(font, line, x, y, color);
            y += LINE_HEIGHT;
        }
        return lines.size() * LINE_HEIGHT;
    }

    /** 按途径分组已学配方 */
    private Map<String, List<Integer>> groupLearnedByPathway(Set<String> learned) {
        Map<String, List<Integer>> grouped = new LinkedHashMap<>();
        for (String entry : learned) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                try {
                    int seq = Integer.parseInt(parts[1]);
                    grouped.computeIfAbsent(parts[0], k -> new ArrayList<>()).add(seq);
                } catch (NumberFormatException ignored) {}
            }
        }
        return grouped;
    }

    /** 将材料 ID 列表翻译为中文显示名 */
    private String translateMaterials(List<String> ids) {
        List<String> translated = new ArrayList<>();
        for (String id : ids) {
            translated.add(translateMaterial(id));
        }
        return String.join("、", translated);
    }

    /** 单个材料 ID → 翻译名 */
    private String translateMaterial(String id) {
        String modid = LordofMysteries.MODID;
        // 尝试主材
        String key = "item." + modid + ".main_material." + id;
        String result = Component.translatable(key).getString();
        if (!result.equals(key)) {
            return result;
        }
        // 尝试辅材
        key = "item." + modid + ".auxiliary_material." + id;
        result = Component.translatable(key).getString();
        if (!result.equals(key)) {
            return result;
        }
        // 纯水特殊处理
        if (id.equals("pure_water")) {
            key = "item." + modid + ".pure_water.pure_water.effect.empty";
            result = Component.translatable(key).getString();
            if (!result.equals(key)) {
                return result;
            }
        }
        return id;
    }

    // ==================== 输入处理 ====================

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_K) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset = Math.clamp(scrollOffset - (int) scrollY * 20, 0, maxScroll);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            int scrollBarX = this.width - 10;
            if (mouseX >= scrollBarX && mouseX <= scrollBarX + 6 && mouseY >= 30 && mouseY <= this.height - 20) {
                isScrolling = true;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            isScrolling = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isScrolling && maxScroll > 0) {
            float ratio = (float) (mouseY - 30) / (this.height - 50);
            scrollOffset = Math.clamp((int) (ratio * maxScroll), 0, maxScroll);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
}
