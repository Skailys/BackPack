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
