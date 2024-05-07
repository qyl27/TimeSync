package cx.rain.mc.timesync.data;

import com.google.gson.Gson;
import cx.rain.mc.timesync.data.model.time.HeFeng;
import cx.rain.mc.timesync.utility.FetchHelper;
import cx.rain.mc.timesync.utility.SunriseHelper;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimeSource implements ConfigurationSerializable {
    private static final Gson GSON = new Gson();

    private final String name;
    private final TimeSourceType type;
    private final int updateInterval;

    private final String timeZone;

    @Nullable
    private Double longitude;
    @Nullable
    private Double latitude;
    @Nullable
    private String apiKey;

    public TimeSource(@Nonnull Map<String, Object> map) {
        name = map.get("name").toString();
        type = TimeSourceType.valueOf(map.get("type").toString());
        updateInterval = Integer.parseInt(map.get("updateInterval").toString());

        timeZone = map.get("timeZone").toString();
        switch (type) {
            case LOCAL -> {
            }
            case SUNRISE_EQUATION -> latitude = Double.parseDouble(map.get("latitude").toString());
            case HE_FENG, HE_FENG_FREE -> {
                longitude = Double.parseDouble(map.get("longitude").toString());
                latitude = Double.parseDouble(map.get("latitude").toString());
                apiKey = map.get("key").toString();
            }
        }
    }

    @Override
    public @Nonnull Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("name", name);
        map.put("type", type);
        map.put("updateInterval", updateInterval);

        map.put("timeZone", timeZone);
        switch (type) {
            case LOCAL -> {
            }
            case SUNRISE_EQUATION -> map.put("latitude", latitude);
            case HE_FENG, HE_FENG_FREE -> {
                map.put("longitude", longitude);
                map.put("latitude", latitude);
                map.put("key", apiKey);
            }
        }
        return map;
    }

    public enum TimeSourceType {
        LOCAL,
        SUNRISE_EQUATION,
        HE_FENG,
        HE_FENG_FREE,
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

    public int fetch() {
        var now = ZonedDateTime.now().withZoneSameInstant(ZoneId.of(getTimeZone()));

        if (getType() == TimeSourceType.LOCAL) {
            var hoursInSec = TimeUnit.HOURS.toSeconds(now.getHour());
            var minutesInSec = TimeUnit.MINUTES.toSeconds(now.getMinute());
            var toClamp = ((int) Math.floor((hoursInSec + minutesInSec + now.getSecond()) / 3.6) + 24000 - 6000) % 24000;
            return Math.max(0, toClamp);
        } else if (getType() == TimeSourceType.SUNRISE_EQUATION) {
            assert latitude != null;
            var hoursInSec = TimeUnit.HOURS.toSeconds(now.getHour());
            var minutesInSec = TimeUnit.MINUTES.toSeconds(now.getMinute());
            var sec = hoursInSec + minutesInSec + now.getSecond();

            var ha = SunriseHelper.sunriseHA(now, latitude);
            var sunrise = SunriseHelper.getSunriseSecond(ha);
            var sunset = SunriseHelper.getSunsetSecond(ha);

            if (sec < sunrise) {
                sec += 43200;
            }

            if (sec < sunset) {
                var tick = (sec - sunrise) * (12000.0 / (sunset - sunrise));
                return (int) Math.max(0, Math.min(tick, 24000));
            } else {
                var tick = (sec - sunset) * (12000.0 / (86400 - sunset + sunrise));
                var tick2 = tick % 24000;
                return (int) Math.max(0, tick2);
            }
        } else if (type == TimeSourceType.HE_FENG) {
            var formatted = String.format("https://api.qweather.com/v7/astronomy/sun?location=%1$.2f,%2$.2f&date=%3$4d%4$2d%5$2d&key=%6$s", longitude, latitude, now.getYear(), now.getMonth().getValue(), now.getDayOfMonth(), apiKey);
            return FetchHelper.fetchJson(formatted, HeFeng.class, true).toMinecraft();
        } else if (type == TimeSourceType.HE_FENG_FREE) {
            var formatted = String.format("https://devapi.qweather.com/v7/astronomy/sun?location=%1$.2f,%2$.2f&date=%3$4d%4$2d%5$2d&key=%6$s", longitude, latitude, now.getYear(), now.getMonth().getValue(), now.getDayOfMonth(), apiKey);
            return FetchHelper.fetchJson(formatted, HeFeng.class, true).toMinecraft();
        }

        throw new RuntimeException("Couldn't fetch time for type " + getType());
    }
}
