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

package dev.skailys.projects.backpacks.config;

import dev.skailys.projects.backpacks.BackPacks;
import lombok.Getter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Logger;

public class ConfigManager {

    private static final String RESOURCE_NOT_FOUND = "Unable to load %s it will be initialize with a default value (%s)";
    private static final String RESOURCE_NOT_PARSED = "Unable to parse %s as %s it will be ignored";

    private static BackPacks instance;
    private static final Logger log = BackPacks.getInstance().getLogger();
    @Getter
    private static FileConfiguration config;
    @Getter
    private static HashMap<String, String> customNames;
    @Getter
    private static ArrayList<GameMode> requiredGameModes;

    public ConfigManager(BackPacks backPack) {
        instance = backPack;
        config = instance.getConfig();
        customNames = loadCustomNames("backpack.name.shulker.custom");
        requiredGameModes = getGamesModes("backpack.bypass.requiredGameMode");
        initValues();
    }

    public static HashMap<String, String> loadCustomNames(String path) {
        ConfigurationSection configurationSection = config.getConfigurationSection(path);
        if (configurationSection == null) {
            log.warning(String.format(RESOURCE_NOT_FOUND, path, null));
            return new HashMap<>();
        }

        Set<String> keys = configurationSection.getKeys(false);
        HashMap<String, String> customNames = new HashMap<>();
        for (String key : keys) {
            customNames.put(key, StringEscapeUtils.unescapeJava(configurationSection.getString(key)));
        }

        return customNames;
    }

    @Nullable
    public static String getCustomNameEntry(@Nonnull String key) {
        return customNames.get(key);
    }

    public static ArrayList<GameMode> getGamesModes(String path) {
        ArrayList<String> raw = (ArrayList<String>) config.get(path);
        ArrayList<GameMode> cache = new ArrayList<>();

        for (String s : raw) {
            try {
                cache.add(GameMode.valueOf(s));
            } catch (Exception e) {
                String warn = String.format(RESOURCE_NOT_PARSED, s, GameMode.class);
                log.warning(warn);
            }
        }

        return cache;
    }

    public static List<String> getStringList(String path) {
        return (ArrayList<String>) config.getList(path);
    }

    public static String getString(String path) {
        return config.getString(path).replace("%nl%", "\n");
    }

    public static Object get(String path) {
        Object value = config.get(path);
        if (value.getClass().equals(String.class)) {
            value = ((String) value).replace("%nl%", "\n");
        }
        return value;
    }

    private void initValues() {
        setDefaultValue("backpack.perm.bypassForceFilter", "backpack.using.bypassForceFilter");
        setDefaultValue("backpack.perm.bypassFiltering", "backpack.using.bypassFiltering");
        setDefaultValue("backpack.bypass.requiredGameMode", Collections.singletonList("CREATIVE"));
        setDefaultValue("backpack.bypass.deactivateForceFilter", false);
        setDefaultValue("backpack.name.shulker.default", "Shulker box");
        setDefaultValue("backpack.warn.notFound.byMaterial", "Could not parse \"%s\" to material");
        setDefaultValue("backpack.warn.cooldown", "§4Don't open the backpacks too fast");
        setDefaultValue("backpack.info.finishedInit", "Backpack (v0.2.3) initializing finished");
        setDefaultValue("backpack.items.forbidden.material", new ArrayList<String>());
        setDefaultValue("backpack.usage.cooldown", 1000);
        setDefaultValue("backpack.watchdog.tps", 17.5);
        setDefaultValue("backpack.watchdog.sleeping", 2400);
        setDefaultValue("backpack.watchdog.startup", "Plugin is now usable");
        setDefaultValue("backpack.watchdog.shutdown.player", "§4Backpacks are currently not available");
        setDefaultValue("backpack.watchdog.shutdown.console", "Plugin was disable because the server tps is too low");
    }

    private void setDefaultValue(String path, Object value) {
        if (config.contains(path)) return;

        config.set(path, value);
        log.warning(String.format(RESOURCE_NOT_FOUND, path, value));
    }
}
