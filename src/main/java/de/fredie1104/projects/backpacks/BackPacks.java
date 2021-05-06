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

package de.fredie1104.projects.backpacks;

import de.fredie1104.projects.backpacks.config.ConfigManager;
import de.fredie1104.projects.backpacks.listener.ModifyShulker;
import de.fredie1104.projects.backpacks.watchdog.Watchdog;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;


public final class BackPacks extends JavaPlugin {

    private static final long DELAY = 0;
    @Getter
    private static BackPacks instance;
    @Getter
    private static BukkitScheduler scheduler;
    @Getter
    private static Watchdog watchdog;
    @Getter
    private static ModifyShulker modifyShulker;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        initConfigManager();

        scheduler = Bukkit.getScheduler();
        watchdog = new Watchdog();
        modifyShulker = new ModifyShulker();

        scheduler.runTask(instance, () -> watchdog.archiveLogs());
        scheduler.runTask(instance, () -> watchdog.run());
        instance.getLogger().info(ConfigManager.getString("backpack.info.finishedInit"));

        long sleeping = (int) ConfigManager.get("backpack.watchdog.sleeping");
        scheduler.runTaskTimer(instance, () -> watchdog.run(), DELAY, sleeping);
        scheduler.runTaskTimer(instance, () -> watchdog.writeCache(), DELAY, sleeping);
        scheduler.runTaskTimer(instance, () -> modifyShulker.cleanCooldowns(), DELAY, sleeping);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        watchdog.panic();
        watchdog.writeCache();
    }

    private void initConfigManager() {
        this.saveDefaultConfig();
        new ConfigManager(this);
    }
}
