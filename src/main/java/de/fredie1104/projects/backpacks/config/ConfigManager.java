package de.fredie1104.projects.backpacks.config;

import de.fredie1104.projects.backpacks.BackPacks;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ConfigManager {

    private static final String RESOURCE_NOT_FOUND = "Unable to load %s it will be initialize with a default (%s)";

    private static BackPacks instance;
    private static final Logger log = BackPacks.getInstance().getLogger();
    @Getter
    private static FileConfiguration config;

    public ConfigManager(BackPacks backPack) {
        instance = backPack;
        config = instance.getConfig();
        initValues();
    }

    public static List<String> getStringList(String path) {
        return (List<String>) config.getList(path);
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
        setDefaultValue("backpack.name.shulker.default", "Shulker box");
        setDefaultValue("backpack.warn.notFound.byMaterial", "Could not parse \"%s\" to material");
        setDefaultValue("backpack.info.finishedInit", "Backpack (v0.2.3) initializing finished");
        setDefaultValue("backpack.items.forbidden.material", new ArrayList<String>());
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
