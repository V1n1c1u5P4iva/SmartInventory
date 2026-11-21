
package com.smartinventory.mod.inventory;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InventoryAnalyzer {

    public static final int TOTAL_SLOTS = 36;
    public static final int ALERT_THRESHOLD = 3;

    private InventoryAnalyzer() {
    }

    public static int countFreeSlots(Inventory inventory) {
        int free = 0;

        for (int slot = 0; slot < TOTAL_SLOTS; slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                free++;
            }
        }

        return free;
    }

    public static boolean isAlmostFull(Inventory inventory) {
        return countFreeSlots(inventory) <= ALERT_THRESHOLD;
    }

    public static InventoryInsight analyze(Inventory inventory) {
        int freeSlots = 0;
        int occupiedSlots = 0;
        int blockSlots = 0;
        int toolSlots = 0;
        int foodSlots = 0;
        int miscSlots = 0;

        for (int slot = 0; slot < TOTAL_SLOTS; slot++) {

            ItemStack stack = inventory.getItem(slot);

            if (stack.isEmpty()) {
                freeSlots++;
                continue;
            }

            occupiedSlots++;

            if (stack.getItem() instanceof BlockItem) {
                blockSlots++;

            } else if (stack.get(DataComponents.TOOL) != null) {
                toolSlots++;

            } else if (stack.get(DataComponents.FOOD) != null) {
                foodSlots++;

            } else {
                miscSlots++;
            }
        }

        String recommendation = buildRecommendation(
                freeSlots,
                blockSlots,
                toolSlots,
                foodSlots,
                miscSlots);

        return new InventoryInsight(
                freeSlots,
                occupiedSlots,
                blockSlots,
                toolSlots,
                foodSlots,
                miscSlots,
                recommendation);
    }

    private static String buildRecommendation(
            int freeSlots,
            int blockSlots,
            int toolSlots,
            int foodSlots,
            int miscSlots) {

        if (freeSlots <= 3) {
            return "message.smartinventory.recommend.almost_full";
        }

        if (foodSlots == 0) {
            return "message.smartinventory.recommend.food";
        }

        if (toolSlots == 0) {
            return "message.smartinventory.recommend.tools";
        }

        if (miscSlots >= 10) {
            return "message.smartinventory.recommend.misc";
        }

        if (blockSlots >= 16) {
            return "message.smartinventory.recommend.blocks";
        }

        return "message.smartinventory.recommend.good";
    }

    public static List<RepeatedItem> findRepeatedItems(
            Inventory inventory) {

        Map<Item, int[]> counts = new HashMap<>();
        Map<Item, String> names = new HashMap<>();
        Map<Item, ItemStack> icons = new HashMap<>();

        for (int slot = 0; slot < TOTAL_SLOTS; slot++) {

            ItemStack stack = inventory.getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            Item item = stack.getItem();

            int[] values = counts.computeIfAbsent(
                    item,
                    key -> new int[2]);

            // Quantidade de slots ocupados
            values[0]++;

            // Total de unidades
            values[1] += stack.getCount();

            names.putIfAbsent(
                    item,
                    stack.getHoverName().getString());

            // Guarda somente uma copia representativa
            // para desenhar o icone.
            icons.putIfAbsent(
                    item,
                    stack.copyWithCount(1));
        }

        List<RepeatedItem> repeated = new ArrayList<>();

        for (Map.Entry<Item, int[]> entry : counts.entrySet()) {

            Item item = entry.getKey();
            int[] values = entry.getValue();

            if (values[0] >= 2) {

                repeated.add(new RepeatedItem(
                        names.get(item),
                        values[0],
                        values[1],
                        icons.get(item)));
            }
        }

        // Mais slots ocupados primeiro
        repeated.sort((a, b) -> {

            int comparison = Integer.compare(
                    b.slots(),
                    a.slots());

            if (comparison != 0) {
                return comparison;
            }

            return a.name().compareToIgnoreCase(b.name());
        });

        return repeated;
    }
}
