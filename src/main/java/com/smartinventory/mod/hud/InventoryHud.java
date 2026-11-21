
package com.smartinventory.mod.hud;

import com.smartinventory.mod.config.HudConfig;
import com.smartinventory.mod.inventory.InventoryAnalyzer;


import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;

public final class InventoryHud {

    private static HudConfig config = HudConfig.load();

    private InventoryHud() {
    }

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HOTBAR,
                Identifier.fromNamespaceAndPath(
                        "smartinventory", "inventory_bar"),
                InventoryHud::render
        );
    }

    public static HudConfig getConfig() {
        return config;
    }

    public static void toggle() {
        config.enabled = !config.enabled;
        config.save();
    }

    public static boolean isEnabled() {
        return config.enabled;
    }

    private static int getColor(float percentage) {
        int level = percentage < 0.70f ? 0
                : percentage < 0.90f ? 1 : 2;

        int[] colors;

        switch (config.palette) {
            case "SOFT":
                colors = new int[]{
                        0xFF76C7B7,
                        0xFFE5BA79,
                        0xFFE88D98
                };
                break;

            case "MONO":
                colors = new int[]{
                        0xFFA8B8CF,
                        0xFFD1D9E5,
                        0xFFF5F7FA
                };
                break;

            default:
                colors = new int[]{
                        0xFF55CC77,
                        0xFFFFCC44,
                        0xFFFF5555
                };
        }

        return colors[level];
    }

    private static void render(
            GuiGraphicsExtractor graphics,
            DeltaTracker delta
    ) {
        Minecraft mc = Minecraft.getInstance();

        if (!config.enabled || mc.player == null) {
            return;
        }

        if (mc.gui.hud.isHidden() || mc.gui.screen() != null) {
            return;
        }

        int total = InventoryAnalyzer.TOTAL_SLOTS;
        int free = InventoryAnalyzer.countFreeSlots(
                mc.player.getInventory());
        int occupied = total - free;

        float percentage = occupied / (float) total;

        int barWidth = switch (config.size) {
            case 1 -> 7;
            case 3 -> 16;
            default -> 10;
        };

        int barHeight = switch (config.size) {
            case 1 -> 55;
            case 3 -> 115;
            default -> 80;
        };

        int x = "LEFT".equals(config.position)
                ? 15 : graphics.guiWidth() - 15 - barWidth;

        int y = (graphics.guiHeight() - barHeight) / 2;

        int color = getColor(percentage);

        graphics.fill(
                x, y,
                x + barWidth, y + barHeight,
                0xAA222222
        );

        int usableHeight = barHeight - 2;
        int filledHeight = Math.round(
                usableHeight * percentage);

        if (filledHeight > 0) {
            graphics.fill(
                    x + 1,
                    y + barHeight - 1 - filledHeight,
                    x + barWidth - 1,
                    y + barHeight - 1,
                    color
            );
        }

        graphics.outline(
                x, y, barWidth, barHeight,
                0xFFFFFFFF
        );

        Component label = Component.translatable(
                "hud.smartinventory.occupancy", occupied, total);

        int textX = "LEFT".equals(config.position)
                ? x + barWidth + 5
                : x - mc.font.width(label) - 5;

        graphics.text(
                mc.font,
                label,
                textX,
                y + barHeight / 2 - 4,
                0xFFFFFFFF,
                true
        );
    }
}



