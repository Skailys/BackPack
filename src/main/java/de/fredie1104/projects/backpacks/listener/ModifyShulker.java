package de.fredie1104.projects.backpacks.listener;


import de.fredie1104.projects.backpacks.filtering.Filtering;
import de.fredie1104.projects.backpacks.qnd.Qnd;
import org.bukkit.Bukkit;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModifyShulker implements Listener {
    final int OFF_HAND_SLOT = 45;

    Set<Player> openedShulkers = new HashSet<>();
    Filtering forbidden = new Filtering();

    private void filteringInventory(Inventory inventory, Player player) {
        if (player.hasPermission("backpacks.using.bypassForceFilter")) {
            return;
        }

        PlayerInventory playerInventory = player.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item == null) {
                continue;
            }

            if (!forbidden.disallowed(item, player)) {
                continue;
            }

            inventory.remove(item);
            Map<Integer, ItemStack> _dropable = playerInventory.addItem(item);

            for (Object itemStack : _dropable.values()) {
                player.getWorld().dropItem(player.getLocation().add(0, 0.5, 0), (ItemStack) itemStack);
            }
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (e.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        if (openedShulkers.contains(p)) {
            return;
        }

        ItemStack item = e.getItem();
        if (item == null) {
            return;
        }

        if (!(item.getItemMeta() instanceof BlockStateMeta)) {
            return;
        }

        BlockStateMeta im = (BlockStateMeta) item.getItemMeta();
        if (!(im.getBlockState() instanceof ShulkerBox)) {
            return;
        }

        String localizedName = im.getLocalizedName();
        String fallbackName = (localizedName.isEmpty()) ? "Shulker box" : localizedName;

        String displayName = im.getDisplayName();
        String backpackName = (displayName.isEmpty()) ? fallbackName : displayName;

        ShulkerBox shulker = (ShulkerBox) im.getBlockState();
        Inventory inv = Bukkit.createInventory(null, 27, backpackName);
        inv.setContents(shulker.getInventory().getContents());

        openedShulkers.add(p);
        p.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();

        if (!openedShulkers.contains(p)) {
            return;
        }
        openedShulkers.remove(p);

        PlayerInventory inv = p.getInventory();
        boolean offHand = Qnd.isShulker(inv.getItemInOffHand());
        boolean mainHand = Qnd.isShulker(inv.getItemInMainHand());

        if (!offHand && !mainHand) {
            return;
        }

        ItemStack origin = (mainHand) ? p.getInventory().getItemInMainHand() : p.getInventory().getItemInOffHand();
        BlockStateMeta bsm = (BlockStateMeta) origin.getItemMeta();
        if (bsm == null) {
            return;
        }

        ShulkerBox box = (ShulkerBox) bsm.getBlockState();

        Inventory eventInventory = e.getInventory();
        filteringInventory(eventInventory, p);

        box.getInventory().setContents(eventInventory.getContents());

        bsm.setBlockState(box);
        box.update();

        int slot = (mainHand) ? p.getInventory().getHeldItemSlot() : OFF_HAND_SLOT;
        origin.setItemMeta(bsm);
        p.getInventory().remove(origin);
        p.getInventory().setItem(slot, origin);
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (!openedShulkers.contains(p)) {
            return;
        }

        Inventory clickedInv = e.getClickedInventory();
        if (clickedInv == null) {
            return;
        }

        PlayerInventory playerInventory = p.getInventory();
        InventoryAction inventoryAction = e.getAction();
        boolean isBackpack = clickedInv.getType() == InventoryType.CHEST;
        boolean isMainHand = Qnd.isShulker(playerInventory.getItemInMainHand());
        boolean eventSlotEqualHand = e.getSlot() == playerInventory.getHeldItemSlot();

        ItemStack currentItem = e.getCurrentItem();
        ItemStack cursor = e.getCursor();

        if (Qnd.isPlace(inventoryAction) && isBackpack) {
            if (forbidden.disallowed(cursor, p)) {
                e.setCancelled(true);
                return;
            }
        }

        if (isMainHand && !isBackpack) {
            if (eventSlotEqualHand) {
                e.setCancelled(true);
                return;
            }
        }

        if (Qnd.isPickup(inventoryAction) && isBackpack) {
            if (forbidden.disallowed(currentItem, p)) {
                e.setCancelled(true);
                //return;
            }
        }

        switch (inventoryAction) {
            case HOTBAR_MOVE_AND_READD:
            case HOTBAR_SWAP:
                int hotbarButton = e.getHotbarButton();
                boolean isSwap = inventoryAction == InventoryAction.HOTBAR_SWAP;

                ItemStack hotbarItem = playerInventory.getItem(hotbarButton);
                if (hotbarItem == null) {
                    return;
                }

                boolean modifyShulkerSlot = Qnd.isShulker(hotbarItem) && isMainHand;
                boolean isIllegalMove = forbidden.disallowed(hotbarItem, p) && !isSwap;

                if (!modifyShulkerSlot && !isIllegalMove) {
                    return;
                }

                break;

            case MOVE_TO_OTHER_INVENTORY:
                if (!forbidden.disallowed(currentItem, p) || isBackpack) {
                    return;
                }
                break;
            case SWAP_WITH_CURSOR:
                if (!forbidden.disallowed(cursor, p) || !isBackpack) {
                    return;
                }
                break;
            default:
                //TODO May cause undefined behavior

                /*
                 * Clone
                 * Collect
                 * Drop
                 * Pickup
                 * Unknown
                 *
                 * */

                //Fallback filter for undefined behavior
                Inventory shulkerInv = e.getView().getTopInventory();
                filteringInventory(shulkerInv, p);
                return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (!openedShulkers.contains(p)) {
            return;
        }

        ItemStack oldCursor = e.getOldCursor();
        boolean isBackpack = Qnd.isInRange(e.getRawSlots(), 0, 26);

        if (!(forbidden.disallowed(oldCursor, p) && isBackpack)) {
            return;
        }

        e.setCancelled(true);
    }
}
