package cx.rain.mc.timesync;

import cx.rain.mc.timesync.config.ConfigManager;
import cx.rain.mc.timesync.data.TimeSource;
import cx.rain.mc.timesync.data.WeatherSource;
import cx.rain.mc.timesync.task.UpdateWorldTimeTask;
import cx.rain.mc.timesync.task.UpdateWorldWeatherTask;
import cx.rain.mc.timesync.task.WorldTimeFliesTask;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TimeSync extends JavaPlugin {
    static {
        ConfigurationSerialization.registerClass(TimeSource.class);
        ConfigurationSerialization.registerClass(WeatherSource.class);
    }

    private final ConfigManager configManager;

    public Map<String, List<Integer>> runnables = new HashMap<>();

    public TimeSync() {
        configManager = new ConfigManager(this);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        for (var world : getServer().getWorlds()) {
            var name = world.getName();
            var time = configManager.getWorldTimeSource(name);
            if (time != null) {
                {
                    var id = getServer().getScheduler().scheduleSyncRepeatingTask(this,
                            new UpdateWorldTimeTask(world, time), 0, time.getUpdateIntervalTicks());

                    if (!runnables.containsKey(name)) {
                        runnables.put(name, new ArrayList<>());
                    }
                    runnables.get(name).add(id);
                }

                {
                    var id = getServer().getScheduler().scheduleSyncRepeatingTask(this,
                            new WorldTimeFliesTask(world), 0, 72);

                    if (!runnables.containsKey(name)) {
                        runnables.put(name, new ArrayList<>());
                    }
                    runnables.get(name).add(id);
                }
            } else {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            }

            var weather = configManager.getWorldWeatherSource(name);
            if (weather != null) {
                var id = getServer().getScheduler().scheduleSyncRepeatingTask(this,
                        new UpdateWorldWeatherTask(world, weather), 0, weather.getUpdateIntervalTicks());

                if (!runnables.containsKey(name)) {
                    runnables.put(name, new ArrayList<>());
                }
                runnables.get(name).add(id);
            } else {
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        runnables.values().forEach(l -> l.forEach(i -> getServer().getScheduler().cancelTask(i)));
    }
}
