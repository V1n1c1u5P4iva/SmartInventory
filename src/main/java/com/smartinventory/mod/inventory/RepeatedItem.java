
package com.smartinventory.mod.inventory;

import net.minecraft.world.item.ItemStack;

public record RepeatedItem(
                String name,
                int slots,
                int units,
                ItemStack icon) {
}
