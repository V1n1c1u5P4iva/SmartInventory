
package com.smartinventory.mod.config;

import com.smartinventory.mod.SmartInventory;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class HudConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("smartinventory-hud.json");

    public boolean enabled = true;
    public String position = "RIGHT";
    public int size = 2;
    public String palette = "CLASSIC";

    public static HudConfig load() {
        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(
                    CONFIG_FILE, StandardCharsets.UTF_8)) {

                HudConfig config = GSON.fromJson(
                        reader, HudConfig.class);

                if (config != null) {
                    config.validate();
                    return config;
                }

            } catch (Exception e) {
                System.err.println(
                        "[SmartInventory] Erro ao ler configuracao: "
                                + e.getMessage());
            }
        }

        return new HudConfig();
    }

    private void validate() {
        if (!"LEFT".equals(position)
                && !"RIGHT".equals(position)) {
            position = "RIGHT";
        }

        size = Math.max(1, Math.min(3, size));

        if (!"CLASSIC".equals(palette)
                && !"SOFT".equals(palette)
                && !"MONO".equals(palette)) {
            palette = "CLASSIC";
        }
    }

    public void save() {
        validate();

        try {
            Files.createDirectories(
                    CONFIG_FILE.getParent());

            try (Writer writer = Files.newBufferedWriter(
                    CONFIG_FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }

        } catch (Exception e) {
            System.err.println(
                    "[SmartInventory] Erro ao salvar configuracao: "
                            + e.getMessage());
        }
    }
}



