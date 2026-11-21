
package com.smartinventory.mod.gui;

import com.smartinventory.mod.config.FavoriteSlots;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FavoriteSlotsScreen extends Screen {

        // Cores do SmartInventory
        private static final int GREEN = 0xFF65D68A;
        private static final int GOLD = 0xFFFFCC55;
        private static final int WHITE = 0xFFFFFFFF;
        private static final int GRAY = 0xFFB5C5B9;

        private static final int PANEL_BG = 0xF0202A24;
        private static final int PANEL_BORDER = 0xFF4B7C58;

        private static final int HOTBAR_BG = 0xFF493E24;
        private static final int INVENTORY_BG = 0xFF293A30;

        private static final int PANEL_WIDTH = 280;
        private static final int PANEL_HEIGHT = 266;

        private static final int SLOT_WIDTH = 24;
        private static final int SLOT_HEIGHT = 20;
        private static final int SLOT_GAP = 2;

        public FavoriteSlotsScreen() {
                super(Component.translatable(
                                "screen.smartinventory.favorites"));
        }

        private int panelTop() {
                return Math.max(
                                6,
                                (height - PANEL_HEIGHT) / 2);
        }

        private int slotsStartX() {
                int totalWidth = 9 * SLOT_WIDTH
                                + 8 * SLOT_GAP;

                return (width - totalWidth) / 2;
        }

        private void addSlotButton(
                        int slot,
                        int x,
                        int y) {

                String label = FavoriteSlots.isFavorite(slot)
                                ? "*"
                                : String.valueOf(slot + 1);

                addRenderableWidget(
                                Button.builder(
                                                Component.literal(label),
                                                button -> {
                                                        FavoriteSlots.toggle(slot);

                                                        minecraft.gui.setScreen(
                                                                        new FavoriteSlotsScreen());
                                                })
                                                .bounds(
                                                                x,
                                                                y,
                                                                SLOT_WIDTH,
                                                                SLOT_HEIGHT)
                                                .build());
        }

        @Override
        protected void init() {
                super.init();

                int top = panelTop();
                int startX = slotsStartX();

                // PRIMEIRA LINHA: HOTBAR (SLOTS 1 A 9)
                for (int slot = 0; slot < 9; slot++) {

                        int x = startX
                                        + slot * (SLOT_WIDTH + SLOT_GAP);

                        addSlotButton(
                                        slot,
                                        x,
                                        top + 64);
                }

                // INVENTARIO PRINCIPAL (SLOTS 10 A 36)
                for (int slot = 9; slot < 36; slot++) {

                        int index = slot - 9;
                        int column = index % 9;
                        int row = index / 9;

                        int x = startX
                                        + column * (SLOT_WIDTH + SLOT_GAP);

                        int y = top + 114
                                        + row * 24;

                        addSlotButton(slot, x, y);
                }

                int centerX = width / 2;

                // Limpar todos os favoritos
                addRenderableWidget(
                                Button.builder(
                                                Component.translatable(
                                                                "screen.smartinventory.clear"),
                                                button -> {
                                                        FavoriteSlots.clear();

                                                        minecraft.gui.setScreen(
                                                                        new FavoriteSlotsScreen());
                                                })
                                                .bounds(
                                                                centerX - 105,
                                                                top + 216,
                                                                100,
                                                                20)
                                                .build());

                // Voltar ao painel principal
                addRenderableWidget(
                                Button.builder(
                                                Component.translatable(
                                                                "screen.smartinventory.back"),
                                                button -> minecraft.gui.setScreen(
                                                                new SmartInventoryScreen()))
                                                .bounds(
                                                                centerX + 5,
                                                                top + 216,
                                                                100,
                                                                20)
                                                .build());
        }

        private void centered(
                        GuiGraphicsExtractor graphics,
                        Component text,
                        int x,
                        int y,
                        int color) {

                graphics.centeredText(
                                font,
                                text,
                                x,
                                y,
                                color);
        }

        @Override
        public void extractRenderState(
                        GuiGraphicsExtractor graphics,
                        int mouseX,
                        int mouseY,
                        float delta) {

                int centerX = width / 2;
                int top = panelTop();
                int left = centerX - PANEL_WIDTH / 2;
                int startX = slotsStartX();

                int gridWidth = 9 * SLOT_WIDTH
                                + 8 * SLOT_GAP;

                // Painel principal
                graphics.fill(
                                left,
                                top,
                                left + PANEL_WIDTH,
                                top + PANEL_HEIGHT,
                                PANEL_BG);

                graphics.outline(
                                left,
                                top,
                                PANEL_WIDTH,
                                PANEL_HEIGHT,
                                PANEL_BORDER);

                // Destaque superior
                graphics.fill(
                                left + 1,
                                top + 1,
                                left + PANEL_WIDTH - 1,
                                top + 4,
                                GREEN);

                // Titulo
                centered(
                                graphics,
                                Component.translatable(
                                                "screen.smartinventory.favorite_slots"),
                                centerX,
                                top + 12,
                                GREEN);

                // Quantidade protegida
                centered(
                                graphics,
                                Component.translatable(
                                                "screen.smartinventory.protected_slots",
                                                FavoriteSlots.count()),
                                centerX,
                                top + 28,
                                WHITE);

                // =========================
                // HOTBAR - AMARELO
                // =========================

                centered(
                                graphics,
                                Component.translatable(
                                                "screen.smartinventory.hotbar_slots"),
                                centerX,
                                top + 47,
                                GOLD);

                graphics.fill(
                                startX - 4,
                                top + 60,
                                startX + gridWidth + 4,
                                top + 88,
                                HOTBAR_BG);

                graphics.outline(
                                startX - 4,
                                top + 60,
                                gridWidth + 8,
                                28,
                                GOLD);

                // =========================
                // INVENTARIO - VERDE
                // =========================

                centered(
                                graphics,
                                Component.translatable(
                                                "screen.smartinventory.main_inventory_slots"),
                                centerX,
                                top + 98,
                                GREEN);

                graphics.fill(
                                startX - 4,
                                top + 110,
                                startX + gridWidth + 4,
                                top + 186,
                                INVENTORY_BG);

                graphics.outline(
                                startX - 4,
                                top + 110,
                                gridWidth + 8,
                                76,
                                PANEL_BORDER);

                // Legenda dos favoritos
                centered(
                                graphics,
                                Component.translatable(
                                                "screen.smartinventory.protected_marker"),
                                centerX,
                                top + 194,
                                GOLD);

                // Separador inferior
                graphics.fill(
                                left + 16,
                                top + 207,
                                left + PANEL_WIDTH - 16,
                                top + 208,
                                PANEL_BORDER);

                // Renderiza os botoes por cima do painel
                super.extractRenderState(
                                graphics,
                                mouseX,
                                mouseY,
                                delta);
        }
}
