package cx.rain.mc.timesync.config;

import cx.rain.mc.timesync.data.TimeSource;
import cx.rain.mc.timesync.data.WeatherSource;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final Plugin plugin;

    private FileConfiguration config;

    private final Map<String, WeatherSource> weatherSourcesWithName = new HashMap<>();
    private final Map<String, TimeSource> timeSourcesWithName = new HashMap<>();

    private final Map<String, WeatherSource> worldWeatherSyncing = new HashMap<>();
    private final Map<String, TimeSource> worldTimeSyncing = new HashMap<>();

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        load();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();

        load();
    }

    private void load() {
        weatherSourcesWithName.clear();
        timeSourcesWithName.clear();
        worldWeatherSyncing.clear();
        worldTimeSyncing.clear();

        var weatherList = config.getList("weatherSource");
        if (weatherList != null) {
            for (var obj : weatherList) {
                if (obj instanceof WeatherSource source) {
                    weatherSourcesWithName.put(source.getName(), source);
                }
            }
        }

        var timeList = config.getList("timeSource");
        if (timeList != null) {
            for (var obj : timeList) {
                if (obj instanceof TimeSource source) {
                    timeSourcesWithName.put(source.getName(), source);
                }
            }
        }

        var weatherWorldSection = config.getConfigurationSection("weatherSyncWorld");
        if (weatherWorldSection == null) {
            return;
        }

        for (var world : weatherWorldSection.getKeys(false)) {
            var sourceName = weatherWorldSection.getString(world);
            var source = weatherSourcesWithName.get(sourceName);
            if (source == null) {
                throw new RuntimeException("Weather source '" + sourceName + "' is missing.");
            }
            worldWeatherSyncing.put(world, source);
        }

        var timeWorldSection = config.getConfigurationSection("timeSyncWorld");
        if (timeWorldSection == null) {
            return;
        }

        for (var world : timeWorldSection.getKeys(false)) {
            var sourceName = timeWorldSection.getString(world);
            var source = timeSourcesWithName.get(sourceName);
            if (source == null) {
                throw new RuntimeException("Time source '" + sourceName + "' is missing.");
            }
            worldTimeSyncing.put(world, source);
        }
    }

    public WeatherSource getWorldWeatherSource(String world) {
        return worldWeatherSyncing.get(world);
    }

    public TimeSource getWorldTimeSource(String world) {
        return worldTimeSyncing.get(world);
    }
}
