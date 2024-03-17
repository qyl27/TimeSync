package cx.rain.mc.timesync.config;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class WeatherSource implements ConfigurationSerializable {
    static {
        ConfigurationSerialization.registerClass(WeatherSource.class);
    }

    private final String name;
    private final WeatherSourceType type;
    private final String key;
    private final double longitude;
    private final double latitude;
    private final int updateInterval;

    public WeatherSource(@Nonnull Map<String, Object> map) {
        name = map.get("name").toString();
        type = WeatherSourceType.valueOf(map.get("type").toString());
        key = map.get("key").toString();
        longitude = Double.parseDouble(map.get("longitude").toString());
        latitude = Double.parseDouble(map.get("latitude").toString());
        updateInterval = Integer.parseInt(map.get("updateInterval").toString());
    }

    @Override
    public @Nonnull Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("name", name);
        map.put("type", type);
        map.put("key", key);
        map.put("longitude", longitude);
        map.put("latitude", latitude);
        map.put("updateInterval", updateInterval);
        return map;
    }

    public enum WeatherSourceType {
        HE_FENG(),
        CAI_YUN(),
        ;

        WeatherSourceType() {
        }
    }

    public String getName() {
        return name;
    }

    public WeatherSourceType getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public double getLongitude() {
        if (getType() == WeatherSourceType.HE_FENG) {
            return (double) Math.round(longitude * 100) / 100;
        }

        return (double) Math.round(longitude * 10000) / 10000;
    }

    public double getLatitude() {
        if (getType() == WeatherSourceType.HE_FENG) {
            return (double) Math.round(latitude * 100) / 100;
        }

        return (double) Math.round(latitude * 10000) / 10000;
    }

    public int getUpdateIntervalTicks() {
        return updateInterval * 20;
    }
}
