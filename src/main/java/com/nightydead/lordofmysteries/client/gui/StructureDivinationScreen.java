package com.nightydead.lordofmysteries.client.gui;

import com.nightydead.lordofmysteries.network.C2SRequestStructuresPacket;
import com.nightydead.lordofmysteries.network.C2SStructureDivinationPacket;
import com.nightydead.lordofmysteries.network.S2CStructureListPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 结构占卜 UI —— 手持指南针+岩石类方块时，长按 C 键选中占卜后弹出的结构列表界面
 * <p>
 * 结构注册表仅在服务端存在，因此 UI 打开后先向服务端请求结构列表，
 * 列表到达后再填充界面。等待期间显示"加载中…"。
 */
public class StructureDivinationScreen extends Screen {

    private static final int LIST_TOP = 45;
    private static final int LIST_BOTTOM_OFFSET = 30;
    private static final int ENTRY_HEIGHT = 22;
    private static final int SCROLL_BAR_WIDTH = 6;

    // ==================== 静态接收缓冲区 ====================

    /** 服务端发来的结构列表，由 {@link S2CStructureListPacket} 处理函数写入 */
    private static List<S2CStructureListPacket.StructureInfo> pendingStructures;
    /** 标记服务端结构列表是否已到达 */
    private static volatile boolean listReceived;

    /**
     * 由 {@link S2CStructureListPacket#handle} 在渲染线程调用，
     * 将服务端发来的结构列表写入静态缓冲区供 UI 读取
     */
    public static void receiveStructureList(List<S2CStructureListPacket.StructureInfo> structures) {
        pendingStructures = structures;
        listReceived = true;
    }

    // ==================== 实例字段 ====================

    private List<StructureEntry> allStructures;
    private List<StructureEntry> filteredStructures;
    private EditBox searchBox;
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private boolean isScrolling = false;
    /** 是否已从静态缓冲区加载过列表 */
    private boolean loaded = false;

    public StructureDivinationScreen() {
        super(Component.translatable("screen.lordofmysteries.structure_divination"));
        this.allStructures = List.of();
        this.filteredStructures = List.of();
        // 向服务端请求结构列表（异步，响应到达后填充列表）
        PacketDistributor.sendToServer(new C2SRequestStructuresPacket());
    }

    // ==================== 初始化 ====================

    @Override
    protected void init() {
        super.init();
        this.searchBox = new EditBox(
                this.font, this.width / 2 - 150, 22, 300, 20,
                Component.translatable("screen.lordofmysteries.structure_divination.search")
        );
        this.searchBox.setMaxLength(64);
        this.searchBox.setResponder(this::onSearchChanged);
        this.addRenderableWidget(this.searchBox);
        this.setInitialFocus(this.searchBox);
    }

    private void onSearchChanged(String text) {
        String query = text.toLowerCase(Locale.ROOT).trim();
        if (query.isEmpty()) {
            filteredStructures = new ArrayList<>(allStructures);
        } else {
            filteredStructures = allStructures.stream()
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
        if (!loaded) return true; // 列表未加载时忽略回车与方向键

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
            if (selectedIndex < filteredStructures.size() - 1) {
                selectedIndex++;
                ensureVisible(selectedIndex);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!loaded) return super.mouseClicked(mouseX, mouseY, button);
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            int scrollBarX = this.width / 2 + 150 + 4;
            if (mouseX >= scrollBarX && mouseX <= scrollBarX + SCROLL_BAR_WIDTH
                    && mouseY >= LIST_TOP && mouseY <= height - LIST_BOTTOM_OFFSET) {
                isScrolling = true;
                return true;
            }

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
        if (isScrolling && loaded) {
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!loaded) return true;
        scrollOffset = Math.clamp(scrollOffset - (int) scrollY * ENTRY_HEIGHT,
                0, Math.max(0, filteredStructures.size() * ENTRY_HEIGHT - getListHeight()));
        return true;
    }

    // ==================== 渲染 ====================

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 检查是否有服务器传来的新列表
        if (!loaded && listReceived && pendingStructures != null) {
            loadFromPending();
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(font, this.title, width / 2, 6, 0xFFCCAA44);

        int listLeft = width / 2 - 150;
        int listRight = width / 2 + 150;
        int listBottom = height - LIST_BOTTOM_OFFSET;
        graphics.fill(listLeft, LIST_TOP, listRight, listBottom, 0x88000000);

        if (!loaded) {
            // 加载中提示
            graphics.drawCenteredString(font,
                    Component.translatable("screen.lordofmysteries.structure_divination.loading"),
                    width / 2, LIST_TOP + 30, 0xFFAAAAAA);
        } else {
            renderStructureList(graphics, mouseX, mouseY, listLeft, listBottom);
        }

        Component hint = Component.translatable("screen.lordofmysteries.structure_divination.hint");
        graphics.drawCenteredString(font, hint, width / 2, height - 20, 0xFF888888);

        if (loaded) {
            Component count = Component.literal(filteredStructures.size() + " / " + allStructures.size());
            graphics.drawString(font, count, width / 2 + 152, height - 20, 0xFF666666);
        }
    }

    private void loadFromPending() {
        List<StructureEntry> entries = new ArrayList<>();
        for (var info : pendingStructures) {
            entries.add(new StructureEntry(info.key(), info.displayName(), info.tags()));
        }
        entries.sort(Comparator.comparing(StructureEntry::displayName));
        this.allStructures = entries;
        this.filteredStructures = new ArrayList<>(entries);
        this.loaded = true;
    }

    private void renderStructureList(GuiGraphics graphics, int mouseX, int mouseY, int listLeft, int listBottom) {
        int listHeight = getListHeight();
        int maxScroll = Math.max(0, filteredStructures.size() * ENTRY_HEIGHT - listHeight);
        scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);

        int startIndex = scrollOffset / ENTRY_HEIGHT;
        int endIndex = Math.min(filteredStructures.size(),
                startIndex + listHeight / ENTRY_HEIGHT + 2);

        for (int i = startIndex; i < endIndex; i++) {
            int y = LIST_TOP + i * ENTRY_HEIGHT - scrollOffset;
            if (y + ENTRY_HEIGHT < LIST_TOP || y > listBottom) continue;

            StructureEntry entry = filteredStructures.get(i);
            boolean isSelected = (i == selectedIndex);
            boolean isHovered = mouseX >= listLeft && mouseX <= listLeft + 300
                    && mouseY >= y && mouseY < y + ENTRY_HEIGHT;

            int bgColor = isSelected ? 0xAA886633 :
                    isHovered ? 0x66444444 : 0x00000000;
            graphics.fill(listLeft, y, listLeft + 300, y + ENTRY_HEIGHT, bgColor);

            int nameColor = isSelected ? 0xFFFFDD88 : 0xFFDDDDDD;
            graphics.drawString(font, entry.displayName(), listLeft + 6, y + 2, nameColor);

            graphics.drawString(font, entry.key().toString(),
                    listLeft + 6, y + 12, 0xFF777777);
        }

        if (maxScroll > 0) {
            int scrollBarX = listLeft + 304;
            int trackHeight = listBottom - LIST_TOP;
            int thumbHeight = Math.max(20, (int) ((float) listHeight / (filteredStructures.size() * ENTRY_HEIGHT) * trackHeight));
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
        if (mouseX < listLeft || mouseX > listLeft + 300) return -1;
        if (mouseY < LIST_TOP || mouseY > listBottom) return -1;
        int relY = (int) mouseY - LIST_TOP + scrollOffset;
        int index = relY / ENTRY_HEIGHT;
        return (index >= 0 && index < filteredStructures.size()) ? index : -1;
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
        int maxScroll = Math.max(0, filteredStructures.size() * ENTRY_HEIGHT - getListHeight());
        if (maxScroll <= 0) return;
        float ratio = (float) (mouseY - trackTop) / trackHeight;
        scrollOffset = (int) (ratio * maxScroll);
        scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);
    }

    // ==================== 确认选择 ====================

    private void confirmSelection() {
        if (selectedIndex >= 0 && selectedIndex < filteredStructures.size()) {
            StructureEntry entry = filteredStructures.get(selectedIndex);
            PacketDistributor.sendToServer(new C2SStructureDivinationPacket(entry.key()));
        }
        onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ==================== 结构条目 ====================

    public record StructureEntry(ResourceLocation key, String displayName, List<String> tags) {

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
