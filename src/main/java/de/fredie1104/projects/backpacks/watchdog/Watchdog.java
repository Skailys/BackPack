package de.fredie1104.projects.backpacks.watchdog;

import de.fredie1104.projects.backpacks.BackPacks;
import de.fredie1104.projects.backpacks.config.ConfigManager;
import de.fredie1104.projects.backpacks.listener.ModifyShulker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;

import java.util.Set;
import java.util.logging.Logger;

public class Watchdog {

    private static final Logger log = BackPacks.getInstance().getLogger();
    private final static double THRESHOLD = (double) ConfigManager.get("backpack.watchdog.tps");
    private static boolean online;

    public void run() {
        if (Bukkit.getServer().getTPS()[0] > THRESHOLD) {
            if (online) return;

            startup();
            online = true;

            String startup = ConfigManager.getString("backpack.watchdog.startup");
            log.info(startup);
            return;
        }

        if (!online) return;
        shutdown();

        String shutdown = ConfigManager.getString("backpack.watchdog.shutdown.console");
        log.warning(shutdown);
    }

    private void startup() {
        PluginManager pm = BackPacks.getInstance().getServer().getPluginManager();
        pm.registerEvents(new ModifyShulker(), BackPacks.getInstance());
        online = true;
    }

    private void shutdown() {
        Set<Player> openedShulkers = ModifyShulker.getOpenedShulkers();

        if (openedShulkers.isEmpty()) {
            HandlerList.unregisterAll(BackPacks.getInstance());
            online = false;
            return;
        }

        String shutdown = ConfigManager.getString("backpack.watchdog.shutdown.player");
        for (Player player : openedShulkers) {
            player.sendMessage(shutdown);
            player.closeInventory();
        }

        HandlerList.unregisterAll(BackPacks.getInstance());
        online = false;
    }
}
