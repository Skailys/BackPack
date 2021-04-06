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

package de.fredie1104.projects.backpacks.filtering;

import de.fredie1104.projects.backpacks.BackPacks;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;

public class Filtering {
    Set<Material> cache = new HashSet<>();

    public Filtering() {
        Logger log = BackPacks.getPlugin().getLogger();

        try {
            File data = new File(BackPacks.getPlugin().getDataFolder(), "filter.csv");
            Scanner dataReader = new Scanner(data);
            while (dataReader.hasNextLine()) {
                String[] rule = dataReader.nextLine().split(",");
                switch (rule[0]) {
                    case "material":
                        Material material = Material.matchMaterial(rule[1]);
                        if (material == null) {
                            log.warning("\"" + rule[1] + "\" couldn't parse to material, by using Material::matchMaterial");
                            break;
                        }
                        cache.add(material);
                        break;

                    default:
                        log.warning("\"" + rule[0] + "\" isn't supported now, more types are coming soon");
                }

            }
        } catch (FileNotFoundException e) {
            log.warning(e.getLocalizedMessage());
        }

    }

    public boolean disallowed(ItemStack itemStack, Player player) {
        if (player.hasPermission("backpacks.using.bypassFiltering")) {
            return false;
        }
        return itemStack != null && cache.contains(itemStack.getType());
    }
}
