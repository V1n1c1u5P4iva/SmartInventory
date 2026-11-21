package com.smartinventory.mod.inventory;

public record InventoryInsight(
        int freeSlots,
        int occupiedSlots,
        int blockSlots,
        int toolSlots,
        int foodSlots,
        int miscSlots,
        String recommendation) {
}


