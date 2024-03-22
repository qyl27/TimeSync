package cx.rain.mc.timesync.data;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import cx.rain.mc.timesync.data.model.CaiYun;
import cx.rain.mc.timesync.data.model.HeFeng;
import cx.rain.mc.timesync.data.model.IWeatherModel;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class WeatherSource implements ConfigurationSerializable {
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
        HE_FENG(HeFeng.class, "https://api.qweather.com/v7/weather/now?location=%2$.2f,%3$.2f&key=%1$s", true),
        HE_FENG_FREE(HeFeng.class, "https://devapi.qweather.com/v7/weather/now?location=%2$.2f,%3$.2f&key=%1$s", true),
        CAI_YUN(CaiYun.class, "https://api.caiyunapp.com/v2.6/%1$s/%2$.4f,%3$.4f/realtime", false),
        ;

        private final Class<? extends IWeatherModel> model;
        private final String pattern;
        private final boolean enforceGzip;

        WeatherSourceType(Class<? extends IWeatherModel> model, String pattern, boolean gzip) {
            this.model = model;
            this.pattern = pattern;
            this.enforceGzip = gzip;
        }

        public Class<? extends IWeatherModel> getModel() {
            return model;
        }

        public String getUrlPattern() {
            return pattern;
        }

        public boolean isEnforceGzip() {
            return enforceGzip;
        }
    }

    public String getName() {
        return name;
    }

    public WeatherSourceType getType() {
        return type;
    }

    private String getKey() {
        return key;
    }

    private double getLongitude() {
        if (getType() == WeatherSourceType.HE_FENG) {
            return (double) Math.round(longitude * 100) / 100;
        }

        return (double) Math.round(longitude * 10000) / 10000;
    }

    private double getLatitude() {
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
            var is = connection.getInputStream();
            if (getType().isEnforceGzip()) {
                is = new GZIPInputStream(is);
            }
            var model = GSON.fromJson(new BufferedReader(new InputStreamReader(is)), getType().getModel());
            return model.toMinecraft();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
