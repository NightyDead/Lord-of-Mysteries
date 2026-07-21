package com.nightydead.lordofmysteries.client.gui;

import com.nightydead.lordofmysteries.network.C2SBiomeDivinationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 群系占卜 UI —— 手持指南针+植物时，长按 C 键选中占卜后弹出的群系列表界面
 * <p>
 * 玩家可通过搜索框按名称或标签筛选群系，选中后发送占卜请求，服务端生成粒子指引
 */
public class BiomeDivinationScreen extends Screen {

    private static final int LIST_TOP = 45;
    private static final int LIST_BOTTOM_OFFSET = 30;
    private static final int ENTRY_HEIGHT = 22;
    private static final int SCROLL_BAR_WIDTH = 6;

    /** 所有已注册群系的缓存列表 */
    private final List<BiomeEntry> allBiomes;
    /** 当前筛选后的群系列表 */
    private List<BiomeEntry> filteredBiomes;
    /** 搜索框 */
    private EditBox searchBox;
    /** 当前选中索引（-1 为未选中） */
    private int selectedIndex = -1;
    /** 列表滚动偏移 */
    private int scrollOffset = 0;
    /** 是否正在拖动滚动条 */
    private boolean isScrolling = false;

    public BiomeDivinationScreen() {
        super(Component.translatable("screen.lordofmysteries.biome_divination"));
        this.allBiomes = buildBiomeList();
        this.filteredBiomes = new ArrayList<>(allBiomes);
    }

    // ==================== 群系数据构建 ====================

    /**
     * 从注册表中读取所有群系，构建带名称和标签的条目列表
     */
    private static List<BiomeEntry> buildBiomeList() {
        var mc = Minecraft.getInstance();
        if (mc.level == null) return List.of();

        var biomeRegistry = mc.level.registryAccess().registryOrThrow(Registries.BIOME);
        List<BiomeEntry> entries = new ArrayList<>();

        for (var entry : biomeRegistry.entrySet()) {
            ResourceLocation key = entry.getKey().location();
            Holder<Biome> holder = biomeRegistry.getHolderOrThrow(
                    biomeRegistry.getResourceKey(entry.getValue()).orElseThrow());

            // 收集该群系的所有标签
            List<String> tags = holder.tags()
                    .map(TagKey::location)
                    .map(ResourceLocation::toString)
                    .collect(Collectors.toList());

            String name = Component.translatable("biome." + key.getNamespace() + "." + key.getPath()).getString();
            entries.add(new BiomeEntry(key, name, tags));
        }

        entries.sort(Comparator.comparing(BiomeEntry::displayName));
        return entries;
    }

    // ==================== 初始化 ====================

    @Override
    protected void init() {
        super.init();
        // 搜索框
        this.searchBox = new EditBox(
                this.font, this.width / 2 - 150, 22, 300, 20,
                Component.translatable("screen.lordofmysteries.biome_divination.search")
        );
        this.searchBox.setMaxLength(64);
        this.searchBox.setResponder(this::onSearchChanged);
        this.addRenderableWidget(this.searchBox);
        this.setInitialFocus(this.searchBox);
    }

    private void onSearchChanged(String text) {
        String query = text.toLowerCase(Locale.ROOT).trim();
        if (query.isEmpty()) {
            filteredBiomes = new ArrayList<>(allBiomes);
        } else {
            filteredBiomes = allBiomes.stream()
                    .filter(e -> e.matches(query))
                    .collect(Collectors.toList());
        }
        selectedIndex = -1;
        scrollOffset = 0;
    }

    // ==================== 输入处理 ====================

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            confirmSelection();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_UP) {
            if (selectedIndex > 0) {
                selectedIndex--;
                ensureVisible(selectedIndex);
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            if (selectedIndex < filteredBiomes.size() - 1) {
                selectedIndex++;
                ensureVisible(selectedIndex);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            // 检查是否点击了滚动条
            int scrollBarX = this.width / 2 + 150 + 4;
            if (mouseX >= scrollBarX && mouseX <= scrollBarX + SCROLL_BAR_WIDTH
                    && mouseY >= LIST_TOP && mouseY <= height - LIST_BOTTOM_OFFSET) {
                isScrolling = true;
                return true;
            }

            // 检查是否点击了列表项
            int clicked = getEntryAt(mouseX, mouseY);
            if (clicked >= 0) {
                selectedIndex = clicked;
                confirmSelection();
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
        if (isScrolling) {
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset = Math.clamp(scrollOffset - (int) scrollY * ENTRY_HEIGHT,
                0, Math.max(0, filteredBiomes.size() * ENTRY_HEIGHT - getListHeight()));
        return true;
    }

    // ==================== 渲染 ====================

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // 标题
        graphics.drawCenteredString(font, this.title, width / 2, 6, 0xFFCCAA44);

        // 列表区域背景
        int listLeft = width / 2 - 150;
        int listRight = width / 2 + 150;
        int listBottom = height - LIST_BOTTOM_OFFSET;
        graphics.fill(listLeft, LIST_TOP, listRight, listBottom, 0x88000000);

        // 渲染群系列表
        renderBiomeList(graphics, mouseX, mouseY, listLeft, listBottom);

        // 底部提示
        Component hint = Component.translatable("screen.lordofmysteries.biome_divination.hint");
        graphics.drawCenteredString(font, hint, width / 2, height - 20, 0xFF888888);

        // 结果计数
        Component count = Component.literal(filteredBiomes.size() + " / " + allBiomes.size());
        graphics.drawString(font, count, width / 2 + 152, height - 20, 0xFF666666);
    }

    private void renderBiomeList(GuiGraphics graphics, int mouseX, int mouseY, int listLeft, int listBottom) {
        int listHeight = getListHeight();
        int maxScroll = Math.max(0, filteredBiomes.size() * ENTRY_HEIGHT - listHeight);
        scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);

        int startIndex = scrollOffset / ENTRY_HEIGHT;
        int endIndex = Math.min(filteredBiomes.size(),
                startIndex + listHeight / ENTRY_HEIGHT + 2);

        // 裁剪区域（简单的逐项渲染）
        for (int i = startIndex; i < endIndex; i++) {
            int y = LIST_TOP + i * ENTRY_HEIGHT - scrollOffset;
            if (y + ENTRY_HEIGHT < LIST_TOP || y > listBottom) continue;

            BiomeEntry entry = filteredBiomes.get(i);
            boolean isSelected = (i == selectedIndex);
            boolean isHovered = mouseX >= listLeft && mouseX <= listLeft + 300
                    && mouseY >= y && mouseY < y + ENTRY_HEIGHT;

            // 背景高亮
            int bgColor = isSelected ? 0xAA886633 :
                    isHovered ? 0x66444444 : 0x00000000;
            graphics.fill(listLeft, y, listLeft + 300, y + ENTRY_HEIGHT, bgColor);

            // 群系名称
            int nameColor = isSelected ? 0xFFFFDD88 : 0xFFDDDDDD;
            graphics.drawString(font, entry.displayName(), listLeft + 6, y + 2, nameColor);

            // 群系 ID（小字灰）
            graphics.drawString(font, entry.key().toString(),
                    listLeft + 6, y + 12, 0xFF777777);
        }

        // 滚动条
        if (maxScroll > 0) {
            int scrollBarX = listLeft + 304;
            int trackHeight = listBottom - LIST_TOP;
            int thumbHeight = Math.max(20, (int) ((float) listHeight / (filteredBiomes.size() * ENTRY_HEIGHT) * trackHeight));
            int thumbY = LIST_TOP + (int) ((float) scrollOffset / maxScroll * (trackHeight - thumbHeight));

            graphics.fill(scrollBarX, LIST_TOP, scrollBarX + SCROLL_BAR_WIDTH, listBottom, 0x44222222);
            graphics.fill(scrollBarX, thumbY, scrollBarX + SCROLL_BAR_WIDTH, thumbY + thumbHeight, 0x88666666);
        }
    }

    // ==================== 滚动辅助 ====================

    private int getListHeight() {
        return height - LIST_BOTTOM_OFFSET - LIST_TOP;
    }

    private int getEntryAt(double mouseX, double mouseY) {
        int listLeft = width / 2 - 150;
        int listBottom = height - LIST_BOTTOM_OFFSET;
        // 必须在列表区域内（包括 Y 轴边界检查，防止搜索框区域误命中）
        if (mouseX < listLeft || mouseX > listLeft + 300) return -1;
        if (mouseY < LIST_TOP || mouseY > listBottom) return -1;
        int relY = (int) mouseY - LIST_TOP + scrollOffset;
        int index = relY / ENTRY_HEIGHT;
        return (index >= 0 && index < filteredBiomes.size()) ? index : -1;
    }

    private void ensureVisible(int index) {
        int y = index * ENTRY_HEIGHT;
        int listHeight = getListHeight();
        if (y < scrollOffset) {
            scrollOffset = y;
        } else if (y + ENTRY_HEIGHT > scrollOffset + listHeight) {
            scrollOffset = y + ENTRY_HEIGHT - listHeight;
        }
    }

    private void updateScrollFromMouse(double mouseY) {
        int trackTop = LIST_TOP;
        int trackBottom = height - LIST_BOTTOM_OFFSET;
        int trackHeight = trackBottom - trackTop;
        int maxScroll = Math.max(0, filteredBiomes.size() * ENTRY_HEIGHT - getListHeight());
        if (maxScroll <= 0) return;
        float ratio = (float) (mouseY - trackTop) / trackHeight;
        scrollOffset = (int) (ratio * maxScroll);
        scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);
    }

    // ==================== 确认选择 ====================

    private void confirmSelection() {
        if (selectedIndex >= 0 && selectedIndex < filteredBiomes.size()) {
            BiomeEntry entry = filteredBiomes.get(selectedIndex);
            PacketDistributor.sendToServer(new C2SBiomeDivinationPacket(entry.key()));
        }
        onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ==================== 群系条目 ====================

    /**
     * 群系条目：封装群系注册键、显示名称、标签列表
     */
    public record BiomeEntry(ResourceLocation key, String displayName, List<String> tags) {

        /**
         * 检查群系是否匹配搜索词：比对显示名称（中文/英文）、注册键、标签
         */
        public boolean matches(String query) {
            if (displayName.toLowerCase(Locale.ROOT).contains(query)) return true;
            if (key.toString().toLowerCase(Locale.ROOT).contains(query)) return true;
            for (String tag : tags) {
                if (tag.toLowerCase(Locale.ROOT).contains(query)) return true;
            }
            return false;
        }
    }
}
