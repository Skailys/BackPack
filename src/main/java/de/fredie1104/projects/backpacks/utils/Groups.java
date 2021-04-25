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

package de.fredie1104.projects.backpacks.utils;

import org.bukkit.block.ShulkerBox;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.Arrays;
import java.util.Set;

//Groups stands for "quick and dirty", here are function which don't pass the other classes
public class Groups {

    private static final InventoryAction[] placeActions =
            {InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME, InventoryAction.PLACE_ALL};
    private static final InventoryAction[] pickupActions =
            {InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_SOME,
                    InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_ALL};
    private static final InventoryAction[] dropActions =
            {InventoryAction.DROP_ONE_CURSOR, InventoryAction.DROP_ALL_CURSOR,
                    InventoryAction.DROP_ONE_SLOT, InventoryAction.DROP_ALL_SLOT};
    private static final InventoryAction[] moveActions =
            {InventoryAction.MOVE_TO_OTHER_INVENTORY, InventoryAction.HOTBAR_MOVE_AND_READD};
    private static final InventoryAction[] collectActions =
            {InventoryAction.COLLECT_TO_CURSOR};
    private static final InventoryAction[] swapActions =
            {InventoryAction.HOTBAR_SWAP, InventoryAction.SWAP_WITH_CURSOR};
    private static final InventoryAction[] cloneActions =
            {InventoryAction.CLONE_STACK};


    public static boolean isShulker(ItemStack item) {
        if (!(item.getItemMeta() instanceof BlockStateMeta)) {
            return false;
        }

        BlockStateMeta im = (BlockStateMeta) item.getItemMeta();
        return im.getBlockState() instanceof ShulkerBox;
    }

    public static boolean isInRange(Set<Integer> rawSlots, Integer includedMin, Integer includedMax) {
        for (Integer rawSlot : rawSlots) {
            if (includedMin <= rawSlot && rawSlot <= includedMax) {
                return true;
            }
        }
        return false;
    }

    public static boolean isClone(InventoryAction inventoryAction) {
        return Arrays.asList(cloneActions).contains(inventoryAction);
    }

    public static boolean isCollect(InventoryAction inventoryAction) {
        return Arrays.asList(collectActions).contains(inventoryAction);
    }

    public static boolean isDrop(InventoryAction inventoryAction) {
        return Arrays.asList(dropActions).contains(inventoryAction);
    }

    public static boolean isMove(InventoryAction inventoryAction) {
        return Arrays.asList(moveActions).contains(inventoryAction);
    }

    public static boolean isPickup(InventoryAction inventoryAction) {
        return Arrays.asList(pickupActions).contains(inventoryAction);
    }

    public static boolean isPlace(InventoryAction inventoryAction) {
        return Arrays.asList(placeActions).contains(inventoryAction);
    }

    public static boolean isSwap(InventoryAction inventoryAction) {
        return Arrays.asList(swapActions).contains(inventoryAction);
    }

}
