/*
    BackPack - Spigot based plugin to use a shulker like a backpacks
    Copyright (C) 2021  fredie04

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package de.fredie1104.projects.backpacks.listener;


import com.destroystokyo.paper.Title;
import de.fredie1104.projects.backpacks.BackPacks;
import de.fredie1104.projects.backpacks.config.ConfigManager;
import de.fredie1104.projects.backpacks.utils.Filtering;
import de.fredie1104.projects.backpacks.utils.Groups;
import de.fredie1104.projects.backpacks.watchdog.Watchdog;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ModifyShulker implements Listener {

    private final static int OFF_HAND_SLOT = 45;
    @Getter
    private static final Set<Player> openedShulkers = new HashSet<>();
    private static final HashMap<Player, Long> playerCooldown = new HashMap<>();
    private static final Filtering forbidden = new Filtering();
    private static Watchdog watchdog = BackPacks.getWatchdog();

    private void filteringInventory(Inventory inventory, Player player) {

        if ((boolean) ConfigManager.get("backpack.bypass.deactivateForceFilter")) {
            return;
        }

        if (ConfigManager.getRequiredGameModes().contains(player.getGameMode()) && player.hasPermission(ConfigManager.getString("backpack.perm.bypassForceFilter"))) {
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

            for (ItemStack itemStack : _dropable.values()) {
                player.getWorld().dropItem(player.getLocation().add(0, 0.5, 0), itemStack);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        PlayerInventory playerInventory = e.getPlayer().getInventory();

        boolean interactHand = Groups.isShulker(playerInventory.getItem(e.getHand()));
        boolean offHand = Groups.isShulker(playerInventory.getItemInOffHand());

        e.setCancelled(interactHand || offHand);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (e.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }


        if (playerCooldown.containsKey(p)) {
            if(Instant.now().toEpochMilli() - playerCooldown.get(p) < (int) ConfigManager.get("backpack.usage.cooldown")) {
                String warning = ConfigManager.getString("backpack.warn.cooldown");
                p.sendActionBar(warning);
                return;
            }
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
        String defaultName = ConfigManager.getString("backpack.name.shulker.default");
        String fallbackName = (localizedName.isEmpty()) ? defaultName : localizedName;

        String displayName = designate(im.getDisplayName());
        String backpackName = (displayName.isEmpty()) ? fallbackName : displayName;

        ShulkerBox shulker = (ShulkerBox) im.getBlockState();
        Inventory inv = Bukkit.createInventory(null, 27, backpackName);
        inv.setContents(shulker.getInventory().getContents());


        Bukkit.getScheduler().runTaskLater(BackPacks.getInstance(), () -> {
            watchdog.log("Open", p, String.valueOf(dumpItemStacks(inv.getContents())));
            openedShulkers.add(p);
            p.openInventory(inv);
        }, 1L);

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();

        if (!openedShulkers.contains(p)) {
            return;
        }
        openedShulkers.remove(p);
        watchdog.log("Close", p, String.valueOf(dumpItemStacks(e.getInventory().getContents())));

        PlayerInventory inv = p.getInventory();
        boolean offHand = Groups.isShulker(inv.getItemInOffHand());
        boolean mainHand = Groups.isShulker(inv.getItemInMainHand());

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

        playerCooldown.put(p, Instant.now().toEpochMilli());

        int slot = (mainHand) ? p.getInventory().getHeldItemSlot() : OFF_HAND_SLOT;
        origin.setItemMeta(bsm);
        p.getInventory().setItem(slot, origin);
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (!openedShulkers.contains(p)) {
            return;
        }

        watchdog.log("Interact", p, String.format("{ %s(%s), %s -> %s, %s}", e.getClickedInventory(), e.getRawSlot(), e.getCurrentItem(), e.getCursor(), e.getAction()));

        Inventory clickedInv = e.getClickedInventory();
        if (clickedInv == null) {
            return;
        }

        PlayerInventory playerInventory = p.getInventory();
        InventoryAction inventoryAction = e.getAction();
        boolean isBackpack = clickedInv.getType() == InventoryType.CHEST;
        boolean isMainHand = Groups.isShulker(playerInventory.getItemInMainHand());
        boolean eventSlotEqualHand = e.getSlot() == playerInventory.getHeldItemSlot();

        ItemStack currentItem = e.getCurrentItem();
        ItemStack cursor = e.getCursor();

        if (Groups.isPlace(inventoryAction) && isBackpack) {
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

        if (Groups.isPickup(inventoryAction) && isBackpack) {
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

                if (hotbarButton == -1) {
                    e.setCancelled(true);
                    return;
                }

                ItemStack hotbarItem = playerInventory.getItem(hotbarButton);
                if (hotbarItem == null) {
                    return;
                }

                boolean modifyShulkerSlot = Groups.isShulker(hotbarItem) && isMainHand;
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
        boolean isBackpack = Groups.isInRange(e.getRawSlots(), 0, 26);
        watchdog.log("Drag", p, String.format("{ Backpack: %s(%s), %s -> %s, %s}", isBackpack, e.getRawSlots(), e.getOldCursor(), e.getCursor(), e.getType()));

        if (!(forbidden.disallowed(oldCursor, p) && isBackpack)) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        p.closeInventory();

        watchdog.log("Death", p, e.getDeathMessage());
    }

    public void cleanCooldowns() {
        int threshold = (int) ConfigManager.get("backpack.usage.cooldown");
        for (Player key : playerCooldown.keySet()) {
            if (key == null) {
                continue;
            }

            if (playerCooldown.get(key) < threshold) {
                continue;
            }
            playerCooldown.remove(key);
        }
    }

    private List<String> dumpItemStacks(ItemStack[] itemStacks) {
        List<String> dumps = new ArrayList<>();
        for (ItemStack itemStack : itemStacks) {
            dumps.add(String.valueOf(itemStack));
        }
        return dumps;
    }

    private String designate(String oldName) {
        String customName = ConfigManager.getCustomNameEntry(oldName);
        return (customName != null) ? customName : oldName;
    }

}
