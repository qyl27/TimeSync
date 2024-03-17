package cx.rain.mc.timesync;

import cx.rain.mc.timesync.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TimeSync extends JavaPlugin {

    private final ConfigManager configManager;

    public TimeSync() {
        configManager = new ConfigManager(this);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
