
package com.smartinventory.mod.gui;

import com.smartinventory.mod.inventory.InventoryAnalyzer;
import com.smartinventory.mod.inventory.RepeatedItem;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RepeatedItemsScreen extends Screen {

        private List<RepeatedItem> items = List.of();

        private static final int GREEN = 0xFF65D68A;
        private static final int WHITE = 0xFFF2F8F3;
        private static final int GRAY = 0xFFB5C5B9;

        private static final int PANEL_BG = 0xF0202A24;
        private static final int PANEL_BORDER = 0xFF4B7C58;
        private static final int CARD_BG = 0xFF2B3B30;
        private static final int CARD_BORDER = 0xFF425C49;

        private static final int PANEL_HEIGHT = 266;
        private static final int ROW_HEIGHT = 29;
        private static final int MAX_VISIBLE = 5;

        public RepeatedItemsScreen() {
                super(Component.translatable(
                                "screen.smartinventory.repeated"));
        }

        private int panelWidth() {
                return Math.min(320, width - 12);
        }

        private int panelTop() {
                return Math.max(
                                4,
                                (height - PANEL_HEIGHT) / 2);
        }

        private void refreshItems() {
                if (minecraft != null && minecraft.player != null) {
                        items = InventoryAnalyzer.findRepeatedItems(
                                        minecraft.player.getInventory());
                }
        }

        @Override
        protected void init() {
                super.init();
                refreshItems();

                int pw = panelWidth();
                int left = (width - pw) / 2;
                int top = panelTop();

                int gap = 8;
                int buttonWidth = (pw - 32) / 2;

                int x1 = left + 12;
                int x2 = x1 + buttonWidth + gap;

                addRenderableWidget(
                                Button.builder(
                                                Component.translatable(
                                                                "screen.smartinventory.refresh"),
                                                button -> refreshItems())
                                                .bounds(
                                                                x1,
                                                                top + 236,
                                                                buttonWidth,
                                                                20)
                                                .build());

                addRenderableWidget(
                                Button.builder(
                                                Component.translatable(
                                                                "screen.smartinventory.back"),
                                                button -> minecraft.gui.setScreen(
                                                                new SmartInventoryScreen()))
                                                .bounds(
                                                                x2,
                                                                top + 236,
                                                                buttonWidth,
                                                                20)
                                                .build());
        }

        private Component fitText(
                        Component component,
                        int maxWidth) {

                String value = component.getString();

                if (font.width(value) <= maxWidth) {
                        return component;
                }

                String shortened = value;

                while (!shortened.isEmpty()
                                && font.width(shortened + "...") > maxWidth) {

                        shortened = shortened.substring(
                                        0,
                                        shortened.length() - 1);
                }

                return Component.literal(shortened + "...");
        }

        private void centered(
                        GuiGraphicsExtractor graphics,
                        Component text,
                        int x,
                        int y,
                        int color,
                        int maxWidth) {

                graphics.centeredText(
                                font,
                                fitText(text, maxWidth),
                                x,
                                y,
                                color);
        }

        private void drawRepeatedItem(
                        GuiGraphicsExtractor graphics,
                        RepeatedItem item,
                        int index,
                        int left,
                        int top,
                        int panelWidth) {

                int rowX = left + 14;
                int rowY = top + 61 + index * ROW_HEIGHT;
                int rowWidth = panelWidth - 28;
                int rowHeight = 25;

                // Fundo da linha
                graphics.fill(
                                rowX,
                                rowY,
                                rowX + rowWidth,
                                rowY + rowHeight,
                                CARD_BG);

                graphics.outline(
                                rowX,
                                rowY,
                                rowWidth,
                                rowHeight,
                                CARD_BORDER);

                // Fundo do icone
                graphics.fill(
                                rowX + 3,
                                rowY + 3,
                                rowX + 23,
                                rowY + 23,
                                0xFF384A3D);

                // Icone real do Minecraft
                if (item.icon() != null && !item.icon().isEmpty()) {
                        graphics.item(
                                        item.icon(),
                                        rowX + 5,
                                        rowY + 5);
                }

                // Nome do item
                int nameX = rowX + 29;
                int availableNameWidth = Math.max(
                                16,
                                rowWidth - 120);

                graphics.text(
                                font,
                                fitText(
                                                Component.literal(item.name()),
                                                availableNameWidth),
                                nameX,
                                rowY + 8,
                                WHITE);

                // Quantidades do lado direito
                int rightX = rowX + rowWidth - 6;

                Component slotsText = Component.translatable(
                                "screen.smartinventory.repeated_slots",
                                item.slots());

                Component unitsText = Component.translatable(
                                "screen.smartinventory.repeated_units",
                                item.units());

                graphics.text(
                                font,
                                slotsText,
                                rightX - font.width(slotsText),
                                rowY + 3,
                                GRAY);

                graphics.text(
                                font,
                                unitsText,
                                rightX - font.width(unitsText),
                                rowY + 14,
                                GREEN);
        }

        @Override
        public void extractRenderState(
                        GuiGraphicsExtractor graphics,
                        int mouseX,
                        int mouseY,
                        float delta) {

                int pw = panelWidth();
                int left = (width - pw) / 2;
                int top = panelTop();
                int cx = width / 2;

                // Fundo principal
                graphics.fill(
                                left,
                                top,
                                left + pw,
                                top + PANEL_HEIGHT,
                                PANEL_BG);

                graphics.outline(
                                left,
                                top,
                                pw,
                                PANEL_HEIGHT,
                                PANEL_BORDER);

                // Barra verde superior
                graphics.fill(
                                left + 1,
                                top + 1,
                                left + pw - 1,
                                top + 4,
                                GREEN);

                // Titulo
                centered(
                                graphics,
                                Component.translatable(
                                                "screen.smartinventory.repeated"),
                                cx,
                                top + 13,
                                GREEN,
                                pw - 24);

                centered(
                                graphics,
                                Component.translatable(
                                                "screen.smartinventory.repeated_hint"),
                                cx,
                                top + 30,
                                GRAY,
                                pw - 24);

                graphics.fill(
                                left + 14,
                                top + 49,
                                left + pw - 14,
                                top + 50,
                                CARD_BORDER);

                if (items.isEmpty()) {

                        centered(
                                        graphics,
                                        Component.translatable(
                                                        "screen.smartinventory.no_repeated_items"),
                                        cx,
                                        top + 115,
                                        WHITE,
                                        pw - 30);

                } else {

                        int limit = Math.min(
                                        MAX_VISIBLE,
                                        items.size());

                        for (int i = 0; i < limit; i++) {
                                drawRepeatedItem(
                                                graphics,
                                                items.get(i),
                                                i,
                                                left,
                                                top,
                                                pw);
                        }

                        // Recomendacao original da tela
                        RepeatedItem first = items.get(0);

                        centered(
                                        graphics,
                                        Component.translatable(
                                                        "screen.smartinventory.recommend_storage",
                                                        first.name()),
                                        cx,
                                        top + 216,
                                        GREEN,
                                        pw - 24);
                }

                // Separador acima dos botoes
                graphics.fill(
                                left + 14,
                                top + 229,
                                left + pw - 14,
                                top + 230,
                                CARD_BORDER);

                // Desenha os botoes depois do painel
                super.extractRenderState(
                                graphics,
                                mouseX,
                                mouseY,
                                delta);
        }
}
