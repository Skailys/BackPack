package de.fredie1104.projects.backpacks;

import de.fredie1104.projects.backpacks.listener.ModifyShulker;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public final class BackPacks extends JavaPlugin {

    private static BackPacks plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ModifyShulker(), this);

        plugin.getLogger().info(plugin.getName()
                + " (v1.0-SNAPSHOT) initializing finished. Read more at github.com/fredie04/<project>/wiki");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static BackPacks getPlugin() {
        return plugin;
    }
}
