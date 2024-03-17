package cx.rain.mc.timesync.config;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class TimeSource implements ConfigurationSerializable {
    static {
        ConfigurationSerialization.registerClass(TimeSource.class);
    }

    private final String name;
    private final TimeSourceType type;
    private final String timeZone;
    private final int updateInterval;

    public TimeSource(@Nonnull Map<String, Object> map) {
        name = map.get("name").toString();
        type = TimeSourceType.valueOf(map.get("type").toString());
        timeZone = map.get("timeZone").toString();
        updateInterval = Integer.parseInt(map.get("updateInterval").toString());
    }

    @Override
    public @Nonnull Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("name", name);
        map.put("type", type);
        map.put("timeZone", timeZone);
        map.put("updateInterval", updateInterval);
        return map;
    }

    public enum TimeSourceType {
        LOCAL,
        ;

        TimeSourceType() {
        }
    }

    public String getName() {
        return name;
    }

    public TimeSourceType getType() {
        return type;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public int getUpdateIntervalTicks() {
        return updateInterval * 20;
    }
}
