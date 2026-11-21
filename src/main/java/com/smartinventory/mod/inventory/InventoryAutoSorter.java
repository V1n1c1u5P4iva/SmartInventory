
package com.smartinventory.mod.inventory;

import com.smartinventory.mod.config.FavoriteSlots;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

public final class InventoryAutoSorter {

    private static final int TOTAL = 36;
    private static final int MOVE_DELAY = 5;

    private static final InventoryAutoSorter INSTANCE = new InventoryAutoSorter();

    private boolean active = false;
    private boolean cancelRequested = false;

    private int target = 0;
    private int delay = 0;
    private int moves = 0;

    private boolean compacted = false;
    private int mergedStacks = 0;
    private int freedSlots = 0;

    private Player owner;
    private ItemStack[] expected;
    private ItemStack[] protectedItems;
    private boolean[] protectedSlots;

    private InventoryAutoSorter() {
    }

    public static InventoryAutoSorter get() {
        return INSTANCE;
    }

    public boolean isActive() {
        return active;
    }

    public void requestCancel() {
        if (active && !cancelRequested) {
            cancelRequested = true;

            message(
                    owner,
                    "message.smartinventory.cancel_requested");
        }
    }

    private static int menuSlot(int inventorySlot) {
        return inventorySlot < 9
                ? inventorySlot + 36
                : inventorySlot;
    }

    private static ItemStack stack(Player player, int slot) {
        return player.getInventory().getItem(slot);
    }

    private static int compare(ItemStack a, ItemStack b) {
        if (a.isEmpty()) {
            return b.isEmpty() ? 0 : 1;
        }

        if (b.isEmpty()) {
            return -1;
        }

        return String.CASE_INSENSITIVE_ORDER.compare(
                a.getHoverName().getString(),
                b.getHoverName().getString());
    }

    private static void message(
            Player player,
            String translationKey,
            Object... args) {

        if (player != null) {
            player.sendOverlayMessage(
                    Component.translatable(
                            translationKey,
                            args));
        }
    }

    public void start(Minecraft mc) {

        if (active
                || mc.player == null
                || mc.gameMode == null) {
            return;
        }

        Player player = mc.player;

        // SmartInventory 1.0: organizacao apenas em mundos locais.
        if (mc.getSingleplayerServer() == null) {
            message(
                    player,
                    "message.smartinventory.singleplayer_only");
            return;
        }

        if (player.hasInfiniteMaterials()) {
            message(
                    player,
                    "message.smartinventory.survival_only");
            return;
        }

        if (player.containerMenu != player.inventoryMenu
                || !player.inventoryMenu.getCarried().isEmpty()) {

            message(
                    player,
                    "message.smartinventory.close_inventory");
            return;
        }

        owner = player;

        expected = new ItemStack[TOTAL];
        protectedItems = new ItemStack[TOTAL];
        protectedSlots = new boolean[TOTAL];

        for (int i = 0; i < TOTAL; i++) {

            ItemStack original = stack(player, i).copy();

            expected[i] = original;
            protectedSlots[i] = FavoriteSlots.isFavorite(i);

            if (protectedSlots[i]) {
                protectedItems[i] = original.copy();
            }
        }

        target = 0;
        delay = 0;
        moves = 0;

        compacted = false;
        mergedStacks = 0;
        freedSlots = 0;

        cancelRequested = false;
        active = true;

        message(
                player,
                "message.smartinventory.started");
    }

    private void stop(
            String translationKey,
            Object... args) {

        active = false;
        cancelRequested = false;

        message(owner, translationKey, args);

        owner = null;
        expected = null;
        protectedItems = null;
        protectedSlots = null;
    }

    /**
     * Verifica se cada favorito continua protegido.
     *
     * Favorito vazio: continua vazio.
     *
     * Favorito ocupado:
     * - mesmo item;
     * - mesmos componentes;
     * - quantidade nunca inferior a inicial;
     * - quantidade dentro do limite da pilha.
     */
    private boolean favoritesValid() {

        for (int i = 0; i < TOTAL; i++) {

            if (FavoriteSlots.isFavorite(i) != protectedSlots[i]) {
                return false;
            }

            if (!protectedSlots[i]) {
                continue;
            }

            ItemStack original = protectedItems[i];
            ItemStack current = stack(owner, i);

            if (original.isEmpty()) {
                if (!current.isEmpty()) {
                    return false;
                }

                continue;
            }

            if (current.isEmpty()) {
                return false;
            }

            if (!ItemStack.isSameItemSameComponents(
                    original,
                    current)) {
                return false;
            }

            if (current.getCount() < original.getCount()) {
                return false;
            }

            if (current.getCount() > current.getMaxStackSize()) {
                return false;
            }
        }

        return true;
    }

    private boolean validState(Minecraft mc) {

        if (mc.player == null
                || mc.player != owner
                || mc.gameMode == null) {
            return false;
        }

        if (owner.containerMenu != owner.inventoryMenu) {
            return false;
        }

        if (!owner.inventoryMenu.getCarried().isEmpty()) {
            return false;
        }

        return favoritesValid();
    }

    /**
     * Verifica se o inventario corresponde exatamente
     * ao resultado esperado da ultima operacao.
     */
    private boolean matchesExpected() {

        for (int i = 0; i < TOTAL; i++) {

            if (!ItemStack.matches(
                    stack(owner, i),
                    expected[i])) {
                return false;
            }
        }

        return true;
    }

    private void click(Minecraft mc, int slot) {

        mc.gameMode.handleContainerInput(
                owner.inventoryMenu.containerId,
                menuSlot(slot),
                0,
                ContainerInput.PICKUP,
                owner);
    }

    /**
     * Executa uma troca apenas entre slots comuns.
     */
    private void swap(Minecraft mc, int a, int b) {

        if (protectedSlots[a] || protectedSlots[b]) {
            return;
        }

        ItemStack first = stack(owner, a).copy();
        ItemStack second = stack(owner, b).copy();

        if (first.isEmpty()) {

            click(mc, b);
            click(mc, a);

        } else {

            click(mc, a);
            click(mc, b);
            click(mc, a);
        }

        expected[a] = second;
        expected[b] = first;

        moves++;
        delay = MOVE_DELAY;
    }

    /**
     * Tenta transferir unidades de source para destination.
     *
     * A origem NUNCA pode ser favorita.
     * O destino pode ser favorito ou comum.
     *
     * O destino precisa conter uma pilha compativel.
     */
    private boolean mergeInto(
            Minecraft mc,
            int destination,
            int source) {

        if (destination == source) {
            return false;
        }

        // Favoritos jamais fornecem itens.
        if (protectedSlots[source]) {
            return false;
        }

        ItemStack targetStack = stack(owner, destination);

        ItemStack sourceStack = stack(owner, source);

        if (targetStack.isEmpty()
                || sourceStack.isEmpty()) {
            return false;
        }

        if (!targetStack.isStackable()
                || !sourceStack.isStackable()) {
            return false;
        }

        if (!ItemStack.isSameItemSameComponents(
                targetStack,
                sourceStack)) {
            return false;
        }

        int limit = targetStack.getMaxStackSize();

        int available = limit - targetStack.getCount();

        if (available <= 0) {
            return false;
        }

        int transferred = Math.min(
                available,
                sourceStack.getCount());

        if (transferred <= 0) {
            return false;
        }

        int remaining = sourceStack.getCount() - transferred;

        ItemStack newTarget = targetStack.copyWithCount(
                targetStack.getCount() + transferred);

        ItemStack newSource = remaining == 0
                ? ItemStack.EMPTY
                : sourceStack.copyWithCount(remaining);

        // Mantem a sequencia de cliques ja existente.
        //
        // 1. Retira itens da origem.
        // 2. Completa o destino.
        // 3. Devolve eventual sobra.

        click(mc, source);
        click(mc, destination);
        click(mc, source);

        // Atualiza a previsao, inclusive dos favoritos.
        expected[destination] = newTarget;
        expected[source] = newSource;

        mergedStacks++;

        if (newSource.isEmpty()) {
            freedSlots++;
        }

        delay = MOVE_DELAY;

        return true;
    }

    /**
     * FASE 1A:
     * Completa os favoritos ocupados primeiro.
     *
     * Procura fontes em todos os slots comuns,
     * inclusive nos anteriores ao favorito.
     */
    private boolean compactFavorites(Minecraft mc) {

        for (int destination = 0; destination < TOTAL; destination++) {

            if (!protectedSlots[destination]) {
                continue;
            }

            ItemStack favorite = stack(owner, destination);

            // Favoritos vazios permanecem vazios.
            if (favorite.isEmpty()
                    || !favorite.isStackable()) {
                continue;
            }

            if (favorite.getCount() >= favorite.getMaxStackSize()) {
                continue;
            }

            for (int source = 0; source < TOTAL; source++) {

                if (protectedSlots[source]) {
                    continue;
                }

                if (mergeInto(
                        mc,
                        destination,
                        source)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * FASE 1B:
     * Compacta os itens restantes em slots comuns.
     *
     * Segue a mesma ordem do planejador.
     */
    private boolean compactNormal(Minecraft mc) {

        for (int destination = 0; destination < TOTAL; destination++) {

            if (protectedSlots[destination]) {
                continue;
            }

            ItemStack targetStack = stack(owner, destination);

            if (targetStack.isEmpty()
                    || !targetStack.isStackable()) {
                continue;
            }

            if (targetStack.getCount() >= targetStack.getMaxStackSize()) {
                continue;
            }

            for (int source = destination + 1; source < TOTAL; source++) {

                if (protectedSlots[source]) {
                    continue;
                }

                if (mergeInto(
                        mc,
                        destination,
                        source)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void tick(Minecraft mc) {

        if (!active) {
            return;
        }

        if (!validState(mc)) {
            stop("message.smartinventory.interrupted");
            return;
        }

        // Aguarda a verificacao da ultima movimentacao.
        if (delay > 0) {

            delay--;

            if (delay == 0) {

                if (!matchesExpected()) {
                    stop(
                            "message.smartinventory.unexpected_change");
                    return;
                }

                if (cancelRequested) {
                    stop(
                            "message.smartinventory.cancelled");
                    return;
                }
            }

            return;
        }

        if (!matchesExpected()) {
            stop(
                    "message.smartinventory.inventory_changed");
            return;
        }

        if (cancelRequested) {
            stop("message.smartinventory.cancelled");
            return;
        }

        // FASE 1: COMPACTACAO
        if (!compacted) {

            // Primeiro completa os favoritos.
            if (compactFavorites(mc)) {
                return;
            }

            // Depois compacta os slots comuns.
            if (compactNormal(mc)) {
                return;
            }

            compacted = true;
        }

        // FASE 2: ORDENACAO ALFABETICA
        while (target < TOTAL) {

            if (protectedSlots[target]) {
                target++;
                continue;
            }

            int best = target;

            for (int i = target + 1; i < TOTAL; i++) {

                if (protectedSlots[i]) {
                    continue;
                }

                if (compare(
                        stack(owner, i),
                        stack(owner, best)) < 0) {
                    best = i;
                }
            }

            int destination = target;
            target++;

            if (best == destination) {
                continue;
            }

            if (compare(
                    stack(owner, destination),
                    stack(owner, best)) == 0) {
                continue;
            }

            swap(mc, destination, best);
            return;
        }

        stop(
                "message.smartinventory.completed",
                freedSlots,
                mergedStacks,
                moves);
    }
}
