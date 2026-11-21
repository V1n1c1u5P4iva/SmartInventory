
package com.smartinventory.mod.config;

import com.smartinventory.mod.SmartInventory;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FavoriteSlots {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("smartinventory-favorites.json");

    private static final int TOTAL = 36;

    private static boolean[] favorites = load();

    private FavoriteSlots() {
    }

    private static boolean valid(int slot) {
        return slot >= 0 && slot < TOTAL;
    }

    public static boolean isFavorite(int slot) {
        return valid(slot) && favorites[slot];
    }

    public static void toggle(int slot) {
        if (!valid(slot)) {
            return;
        }

        favorites[slot] = !favorites[slot];
        save();
    }

    public static int count() {
        int total = 0;

        for (boolean favorite : favorites) {
            if (favorite) {
                total++;
            }
        }

        return total;
    }

    public static void clear() {
        favorites = new boolean[TOTAL];
        save();
    }

    private static boolean[] load() {
        try {
            if (Files.exists(FILE)) {
                String json = Files.readString(
                        FILE, StandardCharsets.UTF_8);

                boolean[] saved = GSON.fromJson(
                        json, boolean[].class);

                if (saved != null && saved.length == TOTAL) {
                    return saved;
                }
            }
        } catch (Exception e) {
            System.err.println(
                    "[SmartInventory] Erro ao carregar favoritos: "
                            + e.getMessage());
        }

        return new boolean[TOTAL];
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());

            Files.writeString(
                    FILE,
                    GSON.toJson(favorites),
                    StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println(
                    "[SmartInventory] Erro ao salvar favoritos: "
                            + e.getMessage());
        }
    }
}



