package de.fredie1104.projects.backpacks.qnd;

import org.bukkit.block.ShulkerBox;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.Arrays;
import java.util.Set;

//Qnd stands for "quick and dirty", here are function which don't pass the other classes
public class Qnd {

    private static InventoryAction[] placeActions =
            {InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME, InventoryAction.PLACE_ALL};
    private static InventoryAction[] pickupActions =
            {InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_SOME,
                    InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_ALL};
    private static InventoryAction[] dropActions =
            {InventoryAction.DROP_ONE_CURSOR, InventoryAction.DROP_ALL_CURSOR,
                    InventoryAction.DROP_ONE_SLOT, InventoryAction.DROP_ALL_SLOT};
    private static InventoryAction[] moveActions =
            {InventoryAction.MOVE_TO_OTHER_INVENTORY, InventoryAction.HOTBAR_MOVE_AND_READD};
    private static InventoryAction[] collectActions =
            {InventoryAction.COLLECT_TO_CURSOR};
    private static InventoryAction[] swapActions =
            {InventoryAction.HOTBAR_SWAP, InventoryAction.SWAP_WITH_CURSOR};
    private static InventoryAction[] cloneActions =
            {InventoryAction.CLONE_STACK};


    public static boolean isShulker(ItemStack item) {
        if (!(item.getItemMeta() instanceof BlockStateMeta)) {
            return false;
        }

        BlockStateMeta im = (BlockStateMeta) item.getItemMeta();
        if (!(im.getBlockState() instanceof ShulkerBox)) {
            return false;
        }

        return true;
    }

    public static boolean isInRange(Set<Integer> rawSlots, Integer includedMin, Integer includedMax) {
        boolean contains = false;
        for (Integer rawSlot : rawSlots) {
            if (includedMin <= rawSlot && rawSlot <= includedMax) {
                contains = true;
            }
        }
        return contains;
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
