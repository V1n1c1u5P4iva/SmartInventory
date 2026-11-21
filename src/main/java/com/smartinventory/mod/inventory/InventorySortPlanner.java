
package com.smartinventory.mod.inventory;

import com.smartinventory.mod.config.FavoriteSlots;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class InventorySortPlanner {

    private static final int TOTAL = 36;

    private InventorySortPlanner() {
    }

    public record Entry(
            int slot,
            String before,
            int beforeCount,
            String after,
            int afterCount,
            boolean protectedSlot) {
    }

    public record Plan(
            List<Entry> entries,
            int freedSlots,
            int merges,
            int swaps,
            ItemStack[] snapshot,
            ItemStack[] result,
            boolean[] favorites) {
    }

    private static String name(ItemStack stack) {
        return stack.isEmpty()
                ? "item.smartinventory.empty"
                : stack.getHoverName().getString();
    }

    private static int compare(ItemStack a, ItemStack b) {
        if (a.isEmpty()) {
            return b.isEmpty() ? 0 : 1;
        }

        if (b.isEmpty()) {
            return -1;
        }

        return String.CASE_INSENSITIVE_ORDER.compare(
                name(a), name(b));
    }

    private static int occupied(ItemStack[] items) {
        int count = 0;

        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Transfere itens de uma origem para um destino compativel.
     *
     * A origem nunca pode ser favorita.
     * O destino pode ser favorito, desde que ja tenha
     * uma pilha compativel.
     */
    private static boolean merge(
            ItemStack[] items,
            boolean[] locked,
            int destination,
            int source) {

        if (destination == source || locked[source]) {
            return false;
        }

        ItemStack target = items[destination];
        ItemStack candidate = items[source];

        if (target.isEmpty() || candidate.isEmpty()) {
            return false;
        }

        if (!target.isStackable() || !candidate.isStackable()) {
            return false;
        }

        if (!ItemStack.isSameItemSameComponents(
                target, candidate)) {
            return false;
        }

        int limit = target.getMaxStackSize();
        int available = limit - target.getCount();

        if (available <= 0) {
            return false;
        }

        int amount = Math.min(
                available,
                candidate.getCount());

        if (amount <= 0) {
            return false;
        }

        int remaining = candidate.getCount() - amount;

        items[destination] = target.copyWithCount(
                target.getCount() + amount);

        items[source] = remaining == 0
                ? ItemStack.EMPTY
                : candidate.copyWithCount(remaining);

        return true;
    }

    public static Plan preview(Inventory inventory) {

        ItemStack[] original = new ItemStack[TOTAL];
        ItemStack[] result = new ItemStack[TOTAL];
        boolean[] locked = new boolean[TOTAL];

        for (int i = 0; i < TOTAL; i++) {
            original[i] = inventory.getItem(i).copy();
            result[i] = original[i].copy();
            locked[i] = FavoriteSlots.isFavorite(i);
        }

        int occupiedBefore = occupied(original);
        int merges = 0;

        // FASE 1A: COMPLETAR SLOTS FAVORITOS

        for (int destination = 0; destination < TOTAL; destination++) {

            if (!locked[destination]) {
                continue;
            }

            ItemStack favorite = result[destination];

            if (favorite.isEmpty() || !favorite.isStackable()) {
                continue;
            }

            if (favorite.getCount() >= favorite.getMaxStackSize()) {
                continue;
            }

            for (int source = 0; source < TOTAL; source++) {

                if (locked[source]) {
                    continue;
                }

                if (merge(
                        result,
                        locked,
                        destination,
                        source)) {
                    merges++;
                }

                ItemStack updated = result[destination];

                if (updated.getCount() >= updated.getMaxStackSize()) {
                    break;
                }
            }
        }

        // FASE 1B: COMPACTAR SLOTS COMUNS

        for (int destination = 0; destination < TOTAL; destination++) {

            if (locked[destination]) {
                continue;
            }

            ItemStack target = result[destination];

            if (target.isEmpty() || !target.isStackable()) {
                continue;
            }

            if (target.getCount() >= target.getMaxStackSize()) {
                continue;
            }

            for (int source = destination + 1; source < TOTAL; source++) {

                if (locked[source]) {
                    continue;
                }

                if (merge(
                        result,
                        locked,
                        destination,
                        source)) {
                    merges++;
                }

                ItemStack updated = result[destination];

                if (updated.getCount() >= updated.getMaxStackSize()) {
                    break;
                }
            }
        }

        int freed = occupiedBefore - occupied(result);

        // FASE 2: ORDENACAO ALFABETICA

        int swaps = 0;

        for (int target = 0; target < TOTAL; target++) {

            if (locked[target]) {
                continue;
            }

            int best = target;

            for (int i = target + 1; i < TOTAL; i++) {

                if (locked[i]) {
                    continue;
                }

                if (compare(result[i], result[best]) < 0) {
                    best = i;
                }
            }

            if (best != target
                    && compare(
                            result[target],
                            result[best]) != 0) {

                ItemStack temp = result[target];
                result[target] = result[best];
                result[best] = temp;

                swaps++;
            }
        }

        // FASE 3: CONSTRUIR A PREVIA

        List<Entry> entries = new ArrayList<>();

        for (int slot = 0; slot < TOTAL; slot++) {

            entries.add(new Entry(
                    slot,
                    name(original[slot]),
                    original[slot].getCount(),
                    name(result[slot]),
                    result[slot].getCount(),
                    locked[slot]));
        }

        return new Plan(
                List.copyOf(entries),
                freed,
                merges,
                swaps,
                original,
                result,
                locked);
    }

    // Confirma que nada mudou desde a previa.
    public static boolean matchesCurrent(
            Plan plan,
            Inventory inventory) {

        for (int slot = 0; slot < TOTAL; slot++) {

            if (FavoriteSlots.isFavorite(slot) != plan.favorites()[slot]) {
                return false;
            }

            if (!ItemStack.matches(
                    plan.snapshot()[slot],
                    inventory.getItem(slot))) {
                return false;
            }
        }

        return true;
    }
}
