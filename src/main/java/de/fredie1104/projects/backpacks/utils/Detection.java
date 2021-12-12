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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class Detection {
    public static boolean isShulker(ItemStack item) {
        if (item == null) {
            return true;
        }

        if (!(item.getItemMeta() instanceof BlockStateMeta)) {
            return true;
        }

        BlockStateMeta im = (BlockStateMeta) item.getItemMeta();
        return !(im.getBlockState() instanceof ShulkerBox);
    }
}
