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

package de.fredie1104.projects.backpacks.watchdog;

import de.fredie1104.projects.backpacks.BackPacks;
import de.fredie1104.projects.backpacks.config.ConfigManager;
import de.fredie1104.projects.backpacks.listener.ModifyShulker;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

public class Watchdog {

    private static final Logger log = BackPacks.getInstance().getLogger();
    private final static double THRESHOLD = (double) ConfigManager.get("backpack.watchdog.tps");
    private final static String DEBUG = "[%s] [%s] Player %s <%s> at %s: %s";
    private static boolean online;

    private static ArrayList<String> cache = new ArrayList<>();

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
    }

    public void writeCache() {
        try {
            File logFile = Path.of(BackPacks.getInstance().getDataFolder().getAbsolutePath(), "latest.log").toFile();

            FileWriter logWriter = new FileWriter(logFile, true);
            BufferedWriter out = new BufferedWriter(logWriter);

            for (String cachedLog : cache) {
                out.write(StringEscapeUtils.unescapeJava(String.format("%s\\n", cachedLog)));
            }
            out.close();

            cache.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void archiveLogs() {
        try {
            File latestLog = Path.of(BackPacks.getInstance().getDataFolder().getAbsolutePath(), "latest.log").toFile();
            if (!latestLog.exists()) {
                return;
            }

            BasicFileAttributes attributes = Files.readAttributes(Path.of(latestLog.getAbsolutePath()), BasicFileAttributes.class);
            FileTime lastInteract = attributes.lastModifiedTime();

            File inputFile = Path.of(BackPacks.getInstance().getDataFolder().getAbsolutePath(), "latest.log").toFile();
            File outputFile = Path.of(BackPacks.getInstance().getDataFolder().getAbsolutePath(), String.format("%s.log.gz", lastInteract.toMillis())).toFile();

            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            BufferedOutputStream outputStream = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(outputFile)));

            int c;
            while ((c = inputStream.read()) != -1) {
                outputStream.write(c);
            }

            if (!latestLog.delete()) log.warning("Could not delete latest.log");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void panic() {
        shutdown();
    }

    public void log(String prefix, Player player, String msg) {
        cache.add(String.format(DEBUG, Instant.now(), prefix, player.getName(), player.getUniqueId(), player.getLocation(), msg));
    }

    private void startup() {
        PluginManager pm = BackPacks.getInstance().getServer().getPluginManager();
        pm.registerEvents(BackPacks.getModifyShulker(), BackPacks.getInstance());

        cache.add(String.format("[%s] [%s] %s", Instant.now(), "Startup", ConfigManager.getString("backpack.watchdog.startup")));
        online = true;
    }

    private void shutdown() {
        Set<Player> openedShulkers = ModifyShulker.getOpenedShulkers();
        cache.add(String.format("[%s] [%s] %s", Instant.now(), "Shutdown", ConfigManager.getString("backpack.watchdog.shutdown.console")));

        String shutdownConsole = ConfigManager.getString("backpack.watchdog.shutdown.console");
        log.warning(shutdownConsole);

        if (openedShulkers.isEmpty()) {
            HandlerList.unregisterAll(BackPacks.getInstance());
            online = false;
            return;
        }

        String shutdownPlayer = ConfigManager.getString("backpack.watchdog.shutdown.player");
        for (Player player : openedShulkers) {
            player.sendMessage(shutdownPlayer);
            player.closeInventory();
        }

        HandlerList.unregisterAll(BackPacks.getInstance());
        online = false;
    }
}
