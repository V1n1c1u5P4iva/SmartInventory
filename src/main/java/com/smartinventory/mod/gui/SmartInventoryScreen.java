
package com.smartinventory.mod.gui;

import com.smartinventory.mod.inventory.InventoryAnalyzer;
import com.smartinventory.mod.inventory.InventoryInsight;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SmartInventoryScreen extends Screen {

        private InventoryInsight insight;

        // Cores principais
        private static final int GREEN = 0xFF65D68A;
        private static final int YELLOW = 0xFFFFCC55;
        private static final int RED = 0xFFFF6666;
        private static final int WHITE = 0xFFF2F8F3;
        private static final int GRAY = 0xFFB5C5B9;

        // Painel
        private static final int PANEL_BG = 0xF0202A24;
        private static final int PANEL_BORDER = 0xFF4B7C58;
        private static final int CARD_BG = 0xFF2B3B30;
        private static final int CARD_BORDER = 0xFF425C49;
        private static final int BAR_BG = 0xFF3E4B41;

        private static final int PANEL_HEIGHT = 268;
        private static final int COMPACT_HEIGHT = 216;

        public SmartInventoryScreen() {
                super(Component.translatable(
                                "screen.smartinventory.title"));
        }

        private boolean compactMode() {
                return this.height < 282;
        }

        private int panelWidth() {
                return Math.min(320, this.width - 12);
        }

        private int panelHeight() {
                return compactMode()
                                ? COMPACT_HEIGHT
                                : PANEL_HEIGHT;
        }

        private int panelTop() {
                return Math.max(
                                4,
                                (height - panelHeight()) / 2);
        }

        private void refreshAnalysis() {
                if (minecraft != null
                                && minecraft.player != null) {

                        insight = InventoryAnalyzer.analyze(
                                        minecraft.player.getInventory());
                }
        }

        private void addButton(
                        String key,
                        int x,
                        int y,
                        int buttonWidth,
                        int buttonHeight,
                        Button.OnPress action) {

                addRenderableWidget(
                                Button.builder(
                                                Component.translatable(key),
                                                action)
                                                .bounds(
                                                                x,
                                                                y,
                                                                buttonWidth,
                                                                buttonHeight)
                                                .build());
        }

        @Override
        protected void init() {
                super.init();

                refreshAnalysis();

                boolean compact = compactMode();

                int pw = panelWidth();
                int left = (width - pw) / 2;
                int top = panelTop();

                int gap = 8;
                int buttonWidth = (pw - 32) / 2;

                int x1 = left + 12;
                int x2 = x1 + buttonWidth + gap;

                int buttonHeight = compact ? 18 : 20;

                int row1 = compact ? 119 : 157;
                int row2 = compact ? 143 : 183;
                int row3 = compact ? 167 : 209;

                // Organizador e favoritos
                addButton(
                                "screen.smartinventory.organize",
                                x1, top + row1,
                                buttonWidth, buttonHeight,
                                b -> minecraft.gui.setScreen(
                                                new InventorySortPreviewScreen()));

                addButton(
                                "screen.smartinventory.favorites",
                                x2, top + row1,
                                buttonWidth, buttonHeight,
                                b -> minecraft.gui.setScreen(
                                                new FavoriteSlotsScreen()));

                // Itens repetidos e HUD
                addButton(
                                "screen.smartinventory.repeated",
                                x1, top + row2,
                                buttonWidth, buttonHeight,
                                b -> minecraft.gui.setScreen(
                                                new RepeatedItemsScreen()));

                addButton(
                                "screen.smartinventory.hud_settings",
                                x2, top + row2,
                                buttonWidth, buttonHeight,
                                b -> minecraft.gui.setScreen(
                                                new HudSettingsScreen()));

                // Atualizar e fechar
                addButton(
                                "screen.smartinventory.refresh",
                                x1, top + row3,
                                buttonWidth, buttonHeight,
                                b -> refreshAnalysis());

                addButton(
                                "screen.smartinventory.close",
                                x2, top + row3,
                                buttonWidth, buttonHeight,
                                b -> minecraft.gui.setScreen(null));
        }

        /**
         * Evita que textos grandes ultrapassem
         * as bordas dos quadros.
         */
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
                        Component component,
                        int x,
                        int y,
                        int color,
                        int maxWidth) {

                graphics.centeredText(
                                font,
                                fitText(component, maxWidth),
                                x, y, color);
        }

        private void centeredKey(
                        GuiGraphicsExtractor graphics,
                        String key,
                        int x,
                        int y,
                        int color,
                        int maxWidth) {

                centered(
                                graphics,
                                Component.translatable(key),
                                x, y, color, maxWidth);
        }

        private int statusColor(float percent) {
                if (percent < 0.70f) {
                        return GREEN;
                }

                if (percent < 0.90f) {
                        return YELLOW;
                }

                return RED;
        }

        private String statusKey(float percent) {
                if (percent >= 0.90f) {
                        return "screen.smartinventory.status.almost_full";
                }

                if (percent >= 0.70f) {
                        return "screen.smartinventory.status.warning";
                }

                return "screen.smartinventory.status.good";
        }

        private void drawCard(
                        GuiGraphicsExtractor graphics,
                        Component label,
                        int x,
                        int y,
                        int width,
                        int height) {

                graphics.fill(
                                x,
                                y,
                                x + width,
                                y + height,
                                CARD_BG);

                graphics.outline(
                                x,
                                y,
                                width,
                                height,
                                CARD_BORDER);

                centered(
                                graphics,
                                label,
                                x + width / 2,
                                y + (height - 8) / 2,
                                WHITE,
                                width - 8);
        }

        @Override
        public void extractRenderState(
                        GuiGraphicsExtractor graphics,
                        int mouseX,
                        int mouseY,
                        float delta) {

                if (insight == null) {
                        super.extractRenderState(
                                        graphics, mouseX, mouseY, delta);
                        return;
                }

                boolean compact = compactMode();

                int pw = panelWidth();
                int ph = panelHeight();
                int left = (width - pw) / 2;
                int top = panelTop();
                int cx = width / 2;

                // Fundo principal
                graphics.fill(
                                left,
                                top,
                                left + pw,
                                top + ph,
                                PANEL_BG);

                graphics.outline(
                                left,
                                top,
                                pw,
                                ph,
                                PANEL_BORDER);

                // Destaque superior
                graphics.fill(
                                left + 1,
                                top + 1,
                                left + pw - 1,
                                top + 4,
                                GREEN);

                // Titulo
                centeredKey(
                                graphics,
                                "screen.smartinventory.brand",
                                cx,
                                top + (compact ? 10 : 15),
                                GREEN,
                                pw - 24);

                centeredKey(
                                graphics,
                                "screen.smartinventory.control_panel",
                                cx,
                                top + (compact ? 23 : 31),
                                GRAY,
                                pw - 24);

                int occupied = insight.occupiedSlots();
                int free = insight.freeSlots();
                int total = InventoryAnalyzer.TOTAL_SLOTS;

                float percent = total > 0
                                ? occupied / (float) total
                                : 0.0f;

                int color = statusColor(percent);

                // Quantidade de slots
                centered(
                                graphics,
                                Component.translatable(
                                                "screen.smartinventory.occupancy",
                                                occupied,
                                                free),
                                cx,
                                top + (compact ? 39 : 50),
                                WHITE,
                                pw - 30);

                // Barra de ocupacao
                int barX = left + 20;
                int barY = top + (compact ? 54 : 68);
                int barWidth = pw - 40;
                int barHeight = 11;

                graphics.fill(
                                barX,
                                barY,
                                barX + barWidth,
                                barY + barHeight,
                                BAR_BG);

                int filledWidth = Math.round(
                                barWidth * percent);

                if (filledWidth > 0) {
                        graphics.fill(
                                        barX,
                                        barY,
                                        barX + filledWidth,
                                        barY + barHeight,
                                        color);
                }

                graphics.outline(
                                barX,
                                barY,
                                barWidth,
                                barHeight,
                                0xFF708C77);

                centered(
                                graphics,
                                Component.translatable(
                                                "screen.smartinventory.occupied_percent",
                                                Math.round(percent * 100)),
                                cx,
                                top + (compact ? 70 : 85),
                                color,
                                pw - 30);

                // Quadros de estatisticas
                int gap = 8;
                int cardWidth = (pw - 40) / 2;
                int cardHeight = compact ? 13 : 18;

                int x1 = left + 16;
                int x2 = x1 + cardWidth + gap;

                int row1 = compact ? 84 : 105;
                int row2 = compact ? 99 : 127;

                drawCard(
                                graphics,
                                Component.translatable(
                                                "screen.smartinventory.blocks",
                                                insight.blockSlots()),
                                x1, top + row1,
                                cardWidth, cardHeight);

                drawCard(
                                graphics,
                                Component.translatable(
                                                "screen.smartinventory.tools",
                                                insight.toolSlots()),
                                x2, top + row1,
                                cardWidth, cardHeight);

                drawCard(
                                graphics,
                                Component.translatable(
                                                "screen.smartinventory.food",
                                                insight.foodSlots()),
                                x1, top + row2,
                                cardWidth, cardHeight);

                drawCard(
                                graphics,
                                Component.translatable(
                                                "screen.smartinventory.other",
                                                insight.miscSlots()),
                                x2, top + row2,
                                cardWidth, cardHeight);

                // Linha divisoria inferior
                int lineY = top + (compact ? 190 : 237);

                graphics.fill(
                                left + 16,
                                lineY,
                                left + pw - 16,
                                lineY + 1,
                                CARD_BORDER);

                // Estado do inventario
                centeredKey(
                                graphics,
                                statusKey(percent),
                                cx,
                                top + (compact ? 199 : 248),
                                color,
                                pw - 24);

                // Botoes nativos do Minecraft
                super.extractRenderState(
                                graphics, mouseX, mouseY, delta);
        }
}
