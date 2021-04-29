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

import de.fredie1104.projects.backpacks.BackPacks;
import de.fredie1104.projects.backpacks.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class Filtering {
    private static final Logger log = BackPacks.getInstance().getLogger();

    Set<Material> cache = new HashSet<>();

    public Filtering() {
        load();
    }

    public boolean disallowed(ItemStack itemStack, Player player) {
        if (ConfigManager.getRequiredGameModes().contains(player.getGameMode()) && player.hasPermission(ConfigManager.getString("backpack.perm.bypassFiltering"))) {
            return false;
        }
        return itemStack != null && cache.contains(itemStack.getType());
    }

    private void load() {
        Logger log = BackPacks.getInstance().getLogger();
        List<String> rawMaterials = ConfigManager.getStringList("backpack.items.forbidden.material");

        for (String item : rawMaterials) {
            Material material = Material.matchMaterial(item);
            if (material == null) {
                String warning = String.format(ConfigManager.getString("backpack.warn.notFound.byMaterial"), item);
                log.warning(warning);
                continue;
            }

            cache.add(material);
        }
    }
}
