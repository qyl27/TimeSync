package cx.rain.mc.timesync.data;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import javax.annotation.Nonnull;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimeSource implements ConfigurationSerializable {
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

    public int fetch() {
        if (getType() == TimeSourceType.LOCAL) {
            var time = ZonedDateTime.now().withZoneSameInstant(ZoneId.of(getTimeZone()));
            var hoursInSec = TimeUnit.HOURS.toSeconds(time.getHour());
            var minutesInSec = TimeUnit.MINUTES.toSeconds(time.getMinute());
            var toClamp = (int) Math.floor((hoursInSec + minutesInSec + time.getSecond()) / 3.6);
            return Math.max(0, Math.min(23999, toClamp));
        }

        throw new RuntimeException("Couldn't fetch time for type " + getType());
    }
}
