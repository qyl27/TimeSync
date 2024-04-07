package cx.rain.mc.timesync.data;

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
    private final String name;
    private final TimeSourceType type;
    private final int updateInterval;

    private String timeZone;
    @Nullable
    private Double latitude;

    public TimeSource(@Nonnull Map<String, Object> map) {
        name = map.get("name").toString();
        type = TimeSourceType.valueOf(map.get("type").toString());
        updateInterval = Integer.parseInt(map.get("updateInterval").toString());

        if (type == TimeSourceType.LOCAL) {
            timeZone = map.get("timeZone").toString();
        } else if (type == TimeSourceType.SUNRISE_EQUATION) {
            timeZone = map.get("timeZone").toString();
            latitude = Double.parseDouble(map.get("latitude").toString());
        }
    }

    @Override
    public @Nonnull Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("name", name);
        map.put("type", type);
        map.put("updateInterval", updateInterval);

        if (type == TimeSourceType.LOCAL) {
            map.put("timeZone", timeZone);
        } else if (type == TimeSourceType.SUNRISE_EQUATION) {
            map.put("timeZone", timeZone);
            map.put("latitude", latitude);
        }
        return map;
    }

    public enum TimeSourceType {
        LOCAL,
        SUNRISE_EQUATION,
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
        if (getType() == TimeSourceType.LOCAL) {
            var time = ZonedDateTime.now().withZoneSameInstant(ZoneId.of(getTimeZone()));
            var hoursInSec = TimeUnit.HOURS.toSeconds(time.getHour());
            var minutesInSec = TimeUnit.MINUTES.toSeconds(time.getMinute());
            var toClamp = ((int) Math.floor((hoursInSec + minutesInSec + time.getSecond()) / 3.6) + 24000 - 6000) % 24000;
            return Math.max(0, toClamp);
        }

        if (getType() == TimeSourceType.SUNRISE_EQUATION) {
            assert latitude != null;
            var time = ZonedDateTime.now().withZoneSameInstant(ZoneId.of(getTimeZone()));
            var hoursInSec = TimeUnit.HOURS.toSeconds(time.getHour());
            var minutesInSec = TimeUnit.MINUTES.toSeconds(time.getMinute());
            var sec = hoursInSec + minutesInSec + time.getSecond();

            var ha = SunriseHelper.sunriseHA(time, latitude);
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
        }

        throw new RuntimeException("Couldn't fetch time for type " + getType());
    }
}
