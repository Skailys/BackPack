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

package dev.skailys.projects.backpacks;

import dev.skailys.projects.backpacks.commands.Backpack;
import dev.skailys.projects.backpacks.config.ConfigManager;
import dev.skailys.projects.backpacks.listener.FallbackListener;
import dev.skailys.projects.backpacks.listener.ModifyShulker;
import dev.skailys.projects.backpacks.watchdog.Watchdog;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Objects;


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
    @Getter
    private static FallbackListener fallback;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        initConfigManager();

        scheduler = Bukkit.getScheduler();
        watchdog = new Watchdog();
        modifyShulker = new ModifyShulker();
        fallback = new FallbackListener();

        scheduler.runTask(instance, () -> watchdog.archiveLogs());
        scheduler.runTask(instance, () -> watchdog.run());
        instance.getLogger().info(ConfigManager.getString("backpack.info.finishedInit"));

        long sleeping = (int) ConfigManager.get("backpack.watchdog.sleeping");
        scheduler.runTaskTimer(instance, () -> watchdog.run(), DELAY, sleeping);
        scheduler.runTaskTimer(instance, () -> watchdog.writeCache(), DELAY, sleeping);
        scheduler.runTaskTimer(instance, () -> modifyShulker.cleanCooldowns(), DELAY, sleeping);

        Objects.requireNonNull(getCommand("backpack")).setExecutor(new Backpack());
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