package com.smartinventory.mod.gui;

import com.smartinventory.mod.config.HudConfig;
import com.smartinventory.mod.hud.InventoryHud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HudSettingsScreen extends Screen {

    private static final int GREEN = 0xFF65D68A;
    private static final int WHITE = 0xFFF2F8F3;
    private static final int GRAY = 0xFFB5C5B9;
    private static final int GOLD = 0xFFFFCC55;

    private static final int PANEL_BG = 0xF0202A24;
    private static final int PANEL_BORDER = 0xFF4B7C58;
    private static final int CARD_BG = 0xFF293A30;
    private static final int CARD_BORDER = 0xFF425C49;
    private static final int SELECTED_BG = 0xFF365342;

    private final HudConfig config = InventoryHud.getConfig();

    public HudSettingsScreen() {
        super(Component.translatable(
                "screen.smartinventory.hud_settings"));
    }

    private int panelWidth() {
        return Math.min(320, Math.max(220, width - 24));
    }

    private int panelHeight() {
        return height < 280 ? 218 : 254;
    }

    private int panelTop() {
        int heightAvailable = panelHeight();
        return Math.max(6, (height - heightAvailable) / 2);
    }

    private int groupY(int top, int row) {
        int compactOffset = height < 280 ? 0 : 8;
        return top + (height < 280 ? 54 : 62)
                + row * (height < 280 ? 32 : 38)
                + compactOffset;
    }

    private void addOptionButton(
            Component label,
            int x,
            int y,
            int buttonWidth,
            Button.OnPress action) {

        addRenderableWidget(
                Button.builder(label, action)
                        .bounds(x, y, buttonWidth, 20)
                        .build());
    }

    private String sizeName() {
        return switch (config.size) {
            case 1 -> "screen.smartinventory.small";
            case 3 -> "screen.smartinventory.large";
            default -> "screen.smartinventory.medium";
        };
    }

    private String paletteName() {
        return switch (config.palette) {
            case "SOFT" -> "screen.smartinventory.soft";
            case "MONO" -> "screen.smartinventory.monochrome";
            default -> "screen.smartinventory.classic";
        };
    }

    private void saveAndRefresh() {
        config.save();
        minecraft.gui.setScreen(new HudSettingsScreen());
    }

    private void restoreDefaults() {
        config.enabled = true;
        config.position = "RIGHT";
        config.size = 2;
        config.palette = "CLASSIC";
        saveAndRefresh();
    }

    @Override
    protected void init() {
        super.init();

        int panelWidth = panelWidth();
        int left = (width - panelWidth) / 2;
        int top = panelTop();

        int groupHeight = height < 280 ? 26 : 30;
        int buttonWidth = Math.min(
                142,
                panelWidth - 78);

        int labelX = left + 14;
        int buttonX = left + panelWidth - 14 - buttonWidth;

        addOptionButton(
                Component.translatable(
                        "screen.smartinventory.hud",
                        Component.translatable(config.enabled
                                ? "screen.smartinventory.enabled"
                                : "screen.smartinventory.disabled")),
                buttonX,
                groupY(top, 0) + (groupHeight - 20) / 2,
                buttonWidth,
                button -> {
                    config.enabled = !config.enabled;
                    saveAndRefresh();
                });

        addOptionButton(
                Component.translatable(
                        "screen.smartinventory.position",
                        Component.translatable("LEFT".equals(config.position)
                                ? "screen.smartinventory.left"
                                : "screen.smartinventory.right")),
                buttonX,
                groupY(top, 1) + (groupHeight - 20) / 2,
                buttonWidth,
                button -> {
                    config.position = "LEFT".equals(config.position)
                            ? "RIGHT"
                            : "LEFT";
                    saveAndRefresh();
                });

        addOptionButton(
                Component.translatable(
                        "screen.smartinventory.size",
                        Component.translatable(sizeName())),
                buttonX,
                groupY(top, 2) + (groupHeight - 20) / 2,
                buttonWidth,
                button -> {
                    config.size = config.size % 3 + 1;
                    saveAndRefresh();
                });

        addOptionButton(
                Component.translatable(
                        "screen.smartinventory.colors",
                        Component.translatable(paletteName())),
                buttonX,
                groupY(top, 3) + (groupHeight - 20) / 2,
                buttonWidth,
                button -> {
                    config.palette = switch (config.palette) {
                        case "CLASSIC" -> "SOFT";
                        case "SOFT" -> "MONO";
                        default -> "CLASSIC";
                    };
                    saveAndRefresh();
                });

        int bottomY = top + panelHeight() - (height < 280 ? 30 : 34);
        int sideGap = 8;
        int bottomWidth = (panelWidth - 32 - sideGap) / 2;

        addRenderableWidget(
                Button.builder(
                        Component.translatable(
                                "screen.smartinventory.restore_defaults"),
                        button -> restoreDefaults())
                        .bounds(
                                left + 16,
                                bottomY,
                                bottomWidth,
                                20)
                        .build());

        addRenderableWidget(
                Button.builder(
                        Component.translatable("screen.smartinventory.back"),
                        button -> minecraft.gui.setScreen(null))
                        .bounds(
                                left + 16 + bottomWidth + sideGap,
                                bottomY,
                                bottomWidth,
                                20)
                        .build());
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

    private void drawOptionGroup(
            GuiGraphicsExtractor graphics,
            int left,
            int top,
            int row,
            Component label,
            boolean selected) {

        int panelWidth = panelWidth();
        int groupHeight = height < 280 ? 26 : 30;
        int y = groupY(top, row);

        graphics.fill(
                left + 12,
                y,
                left + panelWidth - 12,
                y + groupHeight,
                selected ? SELECTED_BG : CARD_BG);

        graphics.outline(
                left + 12,
                y,
                panelWidth - 24,
                groupHeight,
                selected ? GREEN : CARD_BORDER);

        String value = label.getString();
        int labelMaxWidth = panelWidth - 178;

        if (font.width(value) > labelMaxWidth) {
            value = value.substring(
                    0,
                    Math.max(0, value.length() - 1));
            while (!value.isEmpty()
                    && font.width(value + "...") > labelMaxWidth) {
                value = value.substring(
                        0,
                        value.length() - 1);
            }
            if (!value.isEmpty()) {
                value += "...";
            }
        }

        graphics.text(
                font,
                Component.literal(value),
                left + 18,
                y + (groupHeight - font.lineHeight) / 2,
                selected ? WHITE : GRAY,
                false);
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta) {

        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int left = (width - panelWidth) / 2;
        int top = panelTop();
        int centerX = width / 2;

        graphics.fill(
                left,
                top,
                left + panelWidth,
                top + panelHeight,
                PANEL_BG);

        graphics.outline(
                left,
                top,
                panelWidth,
                panelHeight,
                PANEL_BORDER);

        graphics.fill(
                left + 1,
                top + 1,
                left + panelWidth - 1,
                top + 4,
                GREEN);

        centered(
                graphics,
                Component.translatable(
                        "screen.smartinventory.hud_settings"),
                centerX,
                top + (height < 280 ? 9 : 11),
                GREEN,
                panelWidth - 24);

        centered(
                graphics,
                Component.translatable(
                        "screen.smartinventory.hud_settings_hint"),
                centerX,
                top + (height < 280 ? 24 : 29),
                GRAY,
                panelWidth - 24);

        graphics.fill(
                left + 14,
                top + (height < 280 ? 39 : 47),
                left + panelWidth - 14,
                top + (height < 280 ? 40 : 48),
                CARD_BORDER);

        drawOptionGroup(
                graphics,
                left,
                top,
                0,
                Component.translatable(
                        "screen.smartinventory.hud",
                        Component.translatable(config.enabled
                                ? "screen.smartinventory.enabled"
                                : "screen.smartinventory.disabled")),
                config.enabled);

        drawOptionGroup(
                graphics,
                left,
                top,
                1,
                Component.translatable(
                        "screen.smartinventory.position",
                        Component.translatable("LEFT".equals(config.position)
                                ? "screen.smartinventory.left"
                                : "screen.smartinventory.right")),
                true);

        drawOptionGroup(
                graphics,
                left,
                top,
                2,
                Component.translatable(
                        "screen.smartinventory.size",
                        Component.translatable(sizeName())),
                true);

        drawOptionGroup(
                graphics,
                left,
                top,
                3,
                Component.translatable(
                        "screen.smartinventory.colors",
                        Component.translatable(paletteName())),
                true);

        int separatorY = top + panelHeight - (height < 280 ? 38 : 42);
        graphics.fill(
                left + 14,
                separatorY,
                left + panelWidth - 14,
                separatorY + 1,
                CARD_BORDER);

        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                delta);
    }
}
