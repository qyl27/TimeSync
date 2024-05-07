package cx.rain.mc.timesync.data;

import cx.rain.mc.timesync.data.model.weather.CaiYun;
import cx.rain.mc.timesync.data.model.weather.HeFeng;
import cx.rain.mc.timesync.data.model.weather.IWeatherModel;
import cx.rain.mc.timesync.utility.FetchHelper;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class WeatherSource implements ConfigurationSerializable {

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
        return FetchHelper.fetchJson(formatted, getType().getModel(), getType().isEnforceGzip()).toMinecraft();
    }
}
