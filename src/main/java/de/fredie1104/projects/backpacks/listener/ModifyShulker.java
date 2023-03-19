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


import de.fredie1104.projects.backpacks.BackPacks;
import de.fredie1104.projects.backpacks.config.ConfigManager;
import de.fredie1104.projects.backpacks.utils.Detection;
import de.fredie1104.projects.backpacks.utils.Filtering;
import de.fredie1104.projects.backpacks.utils.Groups;
import org.bukkit.Bukkit;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.time.Instant;
import java.util.*;

public class ModifyShulker implements Listener {

//    private final static int OFF_HAND_SLOT = 45;
    private static final Set<Player> openedShulkers = new HashSet<>();
    private static final HashMap<Player, Long> playerCooldown = new HashMap<>();
    private static final Filtering forbidden = new Filtering();

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
//        boolean offHand = Groups.isShulker(playerInventory.getItemInOffHand());

        e.setCancelled(interactHand /* || offHand */);
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

        ItemStack item = p.getInventory().getItemInMainHand();
        if (!Detection.isShulker(item)) return;

        BlockStateMeta im = (BlockStateMeta) item.getItemMeta();
        String localizedName = im.getLocalizedName();
        String defaultName = ConfigManager.getString("backpack.name.shulker.default");
        String fallbackName = (localizedName.isEmpty()) ? defaultName : localizedName;

        String displayName = designate(im.getDisplayName());
        String backpackName = (displayName.isEmpty()) ? fallbackName : displayName;

        ShulkerBox shulker = (ShulkerBox) im.getBlockState();
        Inventory inv = Bukkit.createInventory(null, 27, backpackName);
        inv.setContents(shulker.getInventory().getContents());

        openedShulkers.add(p);
        Bukkit.getScheduler().runTaskLater(BackPacks.getInstance(), () -> {
            p.openInventory(inv);
            writeToMainHand(p.getInventory().getItemInMainHand(), Bukkit.createInventory(null, 27), p);
        }, 1L);

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();

        if (!openedShulkers.contains(p)) {
            return;
        }
        openedShulkers.remove(p);

        PlayerInventory inv = p.getInventory();
        boolean mainHand = Groups.isShulker(inv.getItemInMainHand());

        if (!mainHand) {
            return;
        }

        ItemStack origin = p.getInventory().getItemInMainHand();
        writeToMainHand(origin, e.getInventory(), p);
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
                    ItemStack origin = p.getInventory().getItemInMainHand();
                    writeToMainHand(origin, e.getInventory(), p);
                    return;
                }

                boolean modifyShulkerSlot = Groups.isShulker(hotbarItem) && isMainHand;
                boolean isIllegalMove = forbidden.disallowed(hotbarItem, p) && !isSwap;

                if (!modifyShulkerSlot && !isIllegalMove) {
                    ItemStack origin = p.getInventory().getItemInMainHand();
                    writeToMainHand(origin, e.getInventory(), p);
                    return;
                }

                break;

            case MOVE_TO_OTHER_INVENTORY:
                if (!forbidden.disallowed(currentItem, p) || isBackpack) {
                    ItemStack origin = p.getInventory().getItemInMainHand();
                    writeToMainHand(origin, e.getInventory(), p);
                    return;
                }
                break;
            case SWAP_WITH_CURSOR:
                if (!forbidden.disallowed(cursor, p) || !isBackpack) {
                    ItemStack origin = p.getInventory().getItemInMainHand();
                    writeToMainHand(origin, e.getInventory(), p);
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

        if (!(forbidden.disallowed(oldCursor, p) && isBackpack)) {
            ItemStack origin = p.getInventory().getItemInMainHand();
            writeToMainHand(origin, e.getInventory(), p);
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (!openedShulkers.contains(p)) {
            return;
        }

        p.closeInventory();
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (openedShulkers.contains(p)) {
            return;
        }

        p.closeInventory();
    }

    public void cleanCooldowns() {
        int threshold = (int) ConfigManager.get("backpack.usage.cooldown");
        Set<Player> playerSet = Collections.unmodifiableSet(playerCooldown.keySet());
        for (Player key : playerSet) {
            if (playerCooldown.get(key) < threshold) {
                continue;
            }
            playerCooldown.remove(key);
        }
    }

    public void writeToMainHand(ItemStack origin, Inventory inventory, Player player) {
        BlockStateMeta bsm = (BlockStateMeta) origin.getItemMeta();
        if (bsm == null) {
            return;
        }

        ShulkerBox box = (ShulkerBox) bsm.getBlockState();

        filteringInventory(inventory, player);

        box.getInventory().setContents(inventory.getContents());

        bsm.setBlockState(box);
        box.update();

        playerCooldown.put(player, Instant.now().toEpochMilli());

        int slot = player.getInventory().getHeldItemSlot();
        origin.setItemMeta(bsm);
        player.getInventory().setItem(slot, origin);
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

    public static Set<Player> getOpenedShulkers() {
        return openedShulkers;
    }

}
