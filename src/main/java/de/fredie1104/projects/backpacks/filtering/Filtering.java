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
    Set cache = new HashSet<Material>();

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
        return (itemStack == null) ? false : cache.contains(itemStack.getType());
    }
}
