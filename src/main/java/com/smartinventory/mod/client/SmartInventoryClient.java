
package com.smartinventory.mod.client;

import com.smartinventory.mod.gui.HudSettingsScreen;
import com.smartinventory.mod.gui.SmartInventoryScreen;
import com.smartinventory.mod.hud.InventoryHud;
import com.smartinventory.mod.inventory.InventoryAutoSorter;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class SmartInventoryClient implements ClientModInitializer {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(
                    "smartinventory", "controls"));

    // J - Abrir painel
    private static final KeyMapping OPEN_PANEL = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.smartinventory.open_panel",
                    InputConstants.Type.KEYBOARD,
                    InputConstants.KEY_J,
                    CATEGORY));

    // H - Mostrar ou esconder HUD
    private static final KeyMapping TOGGLE_HUD = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.smartinventory.toggle_hud",
                    InputConstants.Type.KEYBOARD,
                    InputConstants.KEY_H,
                    CATEGORY));

    // K - Configuracoes do HUD
    private static final KeyMapping OPEN_SETTINGS = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.smartinventory.settings",
                    InputConstants.Type.KEYBOARD,
                    InputConstants.KEY_K,
                    CATEGORY));

    // B - Cancelar organizacao
    private static final KeyMapping CANCEL_SORT = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.smartinventory.cancel_sort",
                    InputConstants.Type.KEYBOARD,
                    InputConstants.KEY_B,
                    CATEGORY));

    @Override
    public void onInitializeClient() {

        InventoryHud.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null) {
                return;
            }

            InventoryAutoSorter sorter = InventoryAutoSorter.get();

            // Processa a organizacao em andamento
            sorter.tick(client);

            // J - Painel principal
            while (OPEN_PANEL.consumeClick()) {
                if (client.gui.screen() == null) {
                    client.gui.setScreen(
                            new SmartInventoryScreen());
                }
            }

            // H - HUD
            while (TOGGLE_HUD.consumeClick()) {
                InventoryHud.toggle();
            }

            // K - Configuracoes
            while (OPEN_SETTINGS.consumeClick()) {
                if (client.gui.screen() == null) {
                    client.gui.setScreen(
                            new HudSettingsScreen());
                }
            }

            // B - Cancelamento seguro
            while (CANCEL_SORT.consumeClick()) {
                sorter.requestCancel();
            }
        });
    }
}
