
package com.smartinventory.mod.gui;

import com.smartinventory.mod.inventory.InventoryAutoSorter;
import com.smartinventory.mod.inventory.InventorySortPlanner;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class InventorySortPreviewScreen extends Screen {

        private InventorySortPlanner.Plan plan;
        private String status = "screen.smartinventory.preview_status";

        private static final int GREEN = 0xFF65D68A;
        private static final int WHITE = 0xFFF2F8F3;
        private static final int GRAY = 0xFFB5C5B9;
        private static final int GOLD = 0xFFFFCC55;

        private static final int PANEL_BG = 0xF0202A24;
        private static final int PANEL_BORDER = 0xFF4B7C58;
        private static final int SLOT_BG = 0xFF343D37;
        private static final int SLOT_BORDER = 0xFF68786D;
        private static final int FAVORITE_BG = 0xFF49402B;

        private record Layout(
                        int panelWidth,
                        int panelHeight,
                        int panelTop,
                        int slotSize,
                        int slotGap,
                        int gridWidth,
                        int gridHeight,
                        int firstGridX,
                        int firstGridY,
                        int secondGridX,
                        int secondGridY,
                        int buttonY,
                        boolean stacked) {
        }

        public InventorySortPreviewScreen() {
                super(Component.translatable(
                                "screen.smartinventory.preview"));
        }

        private Layout calculateLayout() {
                int margin = Math.max(4, Math.min(12, height / 24));
                boolean stacked = width < 350;

                int slotSize;
                int slotGap;
                int panelWidth;
                int panelHeight;

                if (stacked) {
                        panelWidth = Math.min(
                                        300,
                                        Math.max(180, width - margin * 2));

                        slotSize = (panelWidth - 24 - 16) / 9;
                        slotSize = Math.max(12, Math.min(18, slotSize));

                        if (height < 300) {
                                slotSize = Math.min(slotSize, 14);
                        }

                        slotGap = slotSize >= 16 ? 2 : 1;

                        int gridHeight = 3 * (slotSize + slotGap)
                                        + 5
                                        + slotSize;

                        panelHeight = height < 300 ? 258 : 296;

                        int top = Math.max(
                                        margin,
                                        (height - panelHeight) / 2);

                        int gridWidth = 9 * slotSize
                                        + 8 * slotGap;

                        int firstGridY = top + 42;

                        int secondGridY = height < 300
                                        ? top + 122
                                        : top + 142;

                        int firstGridX = (width - gridWidth) / 2;
                        int secondGridX = firstGridX;

                        int buttonY = height < 300
                                        ? top + 230
                                        : top + 268;

                        return new Layout(
                                        panelWidth,
                                        panelHeight,
                                        top,
                                        slotSize,
                                        slotGap,
                                        gridWidth,
                                        gridHeight,
                                        firstGridX,
                                        firstGridY,
                                        secondGridX,
                                        secondGridY,
                                        buttonY,
                                        true);
                }

                panelWidth = Math.min(
                                430,
                                Math.max(300, width - margin * 2));

                int availableGridWidth = (panelWidth - 40) / 2;

                slotSize = (availableGridWidth - 16) / 9;
                slotSize = Math.max(12, Math.min(18, slotSize));

                if (height < 250) {
                        slotSize = Math.min(slotSize, 14);
                }

                slotGap = slotSize >= 16 ? 2 : 1;

                int gridWidth = 9 * slotSize
                                + 8 * slotGap;

                int gridHeight = 3 * (slotSize + slotGap)
                                + 5
                                + slotSize;

                panelHeight = height < 250 ? 188 : 206;

                int top = Math.max(
                                margin,
                                (height - panelHeight) / 2);

                int firstGridX = (width - (gridWidth * 2 + 12)) / 2;

                int secondGridX = firstGridX + gridWidth + 12;

                int gridY = top + 45;

                int buttonY = panelHeight == 188
                                ? top + 160
                                : top + 178;

                return new Layout(
                                panelWidth,
                                panelHeight,
                                top,
                                slotSize,
                                slotGap,
                                gridWidth,
                                gridHeight,
                                firstGridX,
                                gridY,
                                secondGridX,
                                gridY,
                                buttonY,
                                false);
        }

        private void refresh() {
                if (minecraft != null && minecraft.player != null) {
                        plan = InventorySortPlanner.preview(
                                        minecraft.player.getInventory());

                        status = "screen.smartinventory.preview_status";
                } else {
                        plan = null;
                        status = "screen.smartinventory.player_unavailable";
                }
        }

        @Override
        protected void init() {
                super.init();

                refresh();

                Layout layout = calculateLayout();

                int left = (width - layout.panelWidth) / 2;
                int innerWidth = layout.panelWidth - 24;
                int gap = 6;

                int confirmWidth = Math.min(130, innerWidth * 42 / 100);
                int refreshWidth = Math.min(88, innerWidth * 32 / 100);

                int backWidth = innerWidth
                                - confirmWidth
                                - refreshWidth
                                - gap * 2;

                backWidth = Math.max(54, backWidth);

                int totalWidth = confirmWidth
                                + refreshWidth
                                + backWidth
                                + gap * 2;

                int startX = left + (layout.panelWidth - totalWidth) / 2;

                addRenderableWidget(
                                Button.builder(
                                                Component.translatable(
                                                                "screen.smartinventory.confirm_organize"),
                                                button -> confirmOrganization())
                                                .bounds(
                                                                startX,
                                                                layout.buttonY,
                                                                confirmWidth,
                                                                20)
                                                .build());

                addRenderableWidget(
                                Button.builder(
                                                Component.translatable(
                                                                "screen.smartinventory.refresh"),
                                                button -> refresh())
                                                .bounds(
                                                                startX + confirmWidth + gap,
                                                                layout.buttonY,
                                                                refreshWidth,
                                                                20)
                                                .build());

                addRenderableWidget(
                                Button.builder(
                                                Component.translatable(
                                                                "screen.smartinventory.back"),
                                                button -> minecraft.gui.setScreen(
                                                                new SmartInventoryScreen()))
                                                .bounds(
                                                                startX + confirmWidth + gap
                                                                                + refreshWidth + gap,
                                                                layout.buttonY,
                                                                backWidth,
                                                                20)
                                                .build());
        }

        private void confirmOrganization() {
                if (plan == null
                                || minecraft == null
                                || minecraft.player == null) {

                        status = "screen.smartinventory.player_unavailable";
                        return;
                }

                if (!InventorySortPlanner.matchesCurrent(
                                plan,
                                minecraft.player.getInventory())) {

                        refresh();
                        status = "screen.smartinventory.inventory_changed";
                        return;
                }

                minecraft.gui.setScreen(null);
                InventoryAutoSorter.get().start(minecraft);
        }

        private void drawInventoryGrid(
                        GuiGraphicsExtractor graphics,
                        ItemStack[] stacks,
                        boolean[] favorites,
                        int originX,
                        int gridY,
                        int mouseX,
                        int mouseY,
                        Layout layout) {

                int size = layout.slotSize;
                int gap = layout.slotGap;
                int step = size + gap;

                graphics.fill(
                                originX - 3,
                                gridY - 3,
                                originX + layout.gridWidth + 3,
                                gridY + layout.gridHeight + 3,
                                0xFF202A24);

                graphics.outline(
                                originX - 3,
                                gridY - 3,
                                layout.gridWidth + 6,
                                layout.gridHeight + 6,
                                PANEL_BORDER);

                for (int visualIndex = 0; visualIndex < 36; visualIndex++) {
                        int row = visualIndex / 9;
                        int column = visualIndex % 9;

                        int inventorySlot = row < 3
                                        ? 9 + row * 9 + column
                                        : column;

                        int slotX = originX + column * step;

                        int slotY = row < 3
                                        ? gridY + row * step
                                        : gridY + 3 * step + 5;

                        boolean favorite = favorites[inventorySlot];

                        int background = favorite
                                        ? FAVORITE_BG
                                        : SLOT_BG;

                        int border = favorite
                                        ? GOLD
                                        : SLOT_BORDER;

                        graphics.fill(
                                        slotX,
                                        slotY,
                                        slotX + size,
                                        slotY + size,
                                        background);

                        graphics.outline(
                                        slotX,
                                        slotY,
                                        size,
                                        size,
                                        border);

                        ItemStack stack = stacks[inventorySlot];

                        if (!stack.isEmpty()) {
                                graphics.item(
                                                stack,
                                                slotX + 1,
                                                slotY + 1);

                                graphics.itemDecorations(
                                                font,
                                                stack,
                                                slotX + 1,
                                                slotY + 1);

                                if (mouseX >= slotX
                                                && mouseX < slotX + size
                                                && mouseY >= slotY
                                                && mouseY < slotY + size) {

                                        graphics.setTooltipForNextFrame(
                                                        font,
                                                        stack,
                                                        mouseX,
                                                        mouseY);
                                }
                        }
                }

                int separatorY = gridY + 3 * step + 2;

                graphics.fill(
                                originX,
                                separatorY,
                                originX + layout.gridWidth,
                                separatorY + 1,
                                PANEL_BORDER);
        }

        private void centered(
                        GuiGraphicsExtractor graphics,
                        Component text,
                        int centerX,
                        int y,
                        int color,
                        int maxWidth) {

                String value = text.getString();

                if (font.width(value) <= maxWidth) {
                        graphics.centeredText(
                                        font,
                                        text,
                                        centerX,
                                        y,
                                        color);
                        return;
                }

                String shortened = value;

                while (!shortened.isEmpty()
                                && font.width(shortened + "...") > maxWidth) {

                        shortened = shortened.substring(
                                        0,
                                        shortened.length() - 1);
                }

                graphics.centeredText(
                                font,
                                Component.literal(shortened + "..."),
                                centerX,
                                y,
                                color);
        }

        @Override
        public void extractRenderState(
                        GuiGraphicsExtractor graphics,
                        int mouseX,
                        int mouseY,
                        float delta) {

                Layout layout = calculateLayout();

                int left = (width - layout.panelWidth) / 2;
                int top = layout.panelTop;
                int centerX = width / 2;

                graphics.fill(
                                left,
                                top,
                                left + layout.panelWidth,
                                top + layout.panelHeight,
                                PANEL_BG);

                graphics.outline(
                                left,
                                top,
                                layout.panelWidth,
                                layout.panelHeight,
                                PANEL_BORDER);

                graphics.fill(
                                left + 1,
                                top + 1,
                                left + layout.panelWidth - 1,
                                top + 4,
                                GREEN);

                centered(
                                graphics,
                                Component.translatable(
                                                "screen.smartinventory.preview_title"),
                                centerX,
                                top + 8,
                                GREEN,
                                layout.panelWidth - 24);

                if (plan != null) {
                        centered(
                                        graphics,
                                        Component.translatable(
                                                        "screen.smartinventory.preview_stats",
                                                        plan.freedSlots(),
                                                        plan.merges(),
                                                        plan.swaps()),
                                        centerX,
                                        top + 23,
                                        WHITE,
                                        layout.panelWidth - 24);
                }

                if (layout.stacked) {
                        centered(
                                        graphics,
                                        Component.translatable(
                                                        "screen.smartinventory.before"),
                                        centerX,
                                        top + 34,
                                        WHITE,
                                        layout.panelWidth - 24);

                        drawPlanGrids(graphics, layout, mouseX, mouseY);

                        centered(
                                        graphics,
                                        Component.translatable(
                                                        "screen.smartinventory.after"),
                                        centerX,
                                        top + (height < 300 ? 114 : 134),
                                        GREEN,
                                        layout.panelWidth - 24);

                        if (plan != null) {
                                drawInventoryGrid(
                                                graphics,
                                                plan.result(),
                                                plan.favorites(),
                                                layout.secondGridX,
                                                layout.secondGridY,
                                                mouseX,
                                                mouseY,
                                                layout);
                        }
                } else {
                        int firstCenter = layout.firstGridX + layout.gridWidth / 2;

                        int secondCenter = layout.secondGridX + layout.gridWidth / 2;

                        centered(
                                        graphics,
                                        Component.translatable(
                                                        "screen.smartinventory.before"),
                                        firstCenter,
                                        top + 34,
                                        WHITE,
                                        layout.gridWidth);

                        centered(
                                        graphics,
                                        Component.translatable(
                                                        "screen.smartinventory.after"),
                                        secondCenter,
                                        top + 34,
                                        GREEN,
                                        layout.gridWidth);

                        drawPlanGrids(graphics, layout, mouseX, mouseY);
                }

                if (plan == null) {
                        centered(
                                        graphics,
                                        Component.translatable(
                                                        "screen.smartinventory.player_unavailable"),
                                        centerX,
                                        layout.firstGridY + 30,
                                        GOLD,
                                        layout.panelWidth - 24);
                }

                int legendY = layout.stacked
                                ? (height < 300 ? top + 194 : top + 232)
                                : (layout.panelHeight == 188
                                                ? top + 128
                                                : top + 136);

                centered(
                                graphics,
                                Component.translatable(
                                                "screen.smartinventory.preview_hotbar"),
                                centerX,
                                legendY,
                                GOLD,
                                layout.panelWidth - 24);

                centered(
                                graphics,
                                Component.translatable(
                                                "screen.smartinventory.preview_favorite"),
                                centerX,
                                legendY + 12,
                                GRAY,
                                layout.panelWidth - 24);

                graphics.fill(
                                left + 14,
                                legendY + 23,
                                left + layout.panelWidth - 14,
                                legendY + 24,
                                PANEL_BORDER);

                centered(
                                graphics,
                                Component.translatable(status),
                                centerX,
                                legendY + 29,
                                GOLD,
                                layout.panelWidth - 24);

                super.extractRenderState(
                                graphics,
                                mouseX,
                                mouseY,
                                delta);
        }

        private void drawPlanGrids(
                        GuiGraphicsExtractor graphics,
                        Layout layout,
                        int mouseX,
                        int mouseY) {

                if (plan == null) {
                        return;
                }

                drawInventoryGrid(
                                graphics,
                                plan.snapshot(),
                                plan.favorites(),
                                layout.firstGridX,
                                layout.firstGridY,
                                mouseX,
                                mouseY,
                                layout);

                if (!layout.stacked) {
                        drawInventoryGrid(
                                        graphics,
                                        plan.result(),
                                        plan.favorites(),
                                        layout.secondGridX,
                                        layout.secondGridY,
                                        mouseX,
                                        mouseY,
                                        layout);
                }
        }
}
