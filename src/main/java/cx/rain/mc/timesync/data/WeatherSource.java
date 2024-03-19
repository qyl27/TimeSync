package cx.rain.mc.timesync.data;

import com.google.gson.Gson;
import cx.rain.mc.timesync.data.model.CaiYun;
import cx.rain.mc.timesync.data.model.HeFeng;
import cx.rain.mc.timesync.data.model.IWeatherModel;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class WeatherSource implements ConfigurationSerializable {
    static {
        ConfigurationSerialization.registerClass(WeatherSource.class);
    }

    private static final Gson GSON = new Gson();

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
        HE_FENG(HeFeng.class, "https://api.qweather.com/v7/weather/now?location={2},{3}&key={1}"),
        HE_FENG_FREE(HeFeng.class, "https://devapi.qweather.com/v7/weather/now?location={2},{3}&key={1}"),
        CAI_YUN(CaiYun.class, "https://api.caiyunapp.com/v2.6/{1}/{2},{3}/realtime"),
        ;

        private final Class<? extends IWeatherModel> model;
        private final String pattern;

        WeatherSourceType(Class<? extends IWeatherModel> model, String pattern) {
            this.model = model;
            this.pattern = pattern;
        }

        public Class<? extends IWeatherModel> getModel() {
            return model;
        }

        public String getUrlPattern() {
            return pattern;
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

    public WeatherType fetch() {
        var formatted = String.format(getType().getUrlPattern(), getKey(), getLongitude(), getLatitude());
        try {
            var url = new URL(formatted);
            var connection = url.openConnection();
            var model = GSON.fromJson(new InputStreamReader(connection.getInputStream()), getType().getModel());
            return model.toMinecraft();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
