package cx.rain.mc.timesync.data;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.JulianFields;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimeSource implements ConfigurationSerializable {
    private final String name;
    private final TimeSourceType type;

    // Nullable start
    private String timeZone;
    private double longitude;
    private double latitude;
    // Nullable end

    private final int updateInterval;

    public TimeSource(@Nonnull Map<String, Object> map) {
        name = map.get("name").toString();
        type = TimeSourceType.valueOf(map.get("type").toString());

        switch (type) {
            case LOCAL -> {
                timeZone = map.get("timeZone").toString();
            }
            case ASTRO_SUNRISE -> {
                longitude = Double.parseDouble(map.get("longitude").toString());
                latitude = Double.parseDouble(map.get("latitude").toString());
            }
        }

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
        ASTRO_SUNRISE,
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

    public int getUpdateIntervalTicks() {
        return updateInterval * 20;
    }

    public int fetch() {
        if (getType() == TimeSourceType.LOCAL) {
            var time = ZonedDateTime.now().withZoneSameInstant(ZoneId.of(timeZone));
            var hoursInSec = TimeUnit.HOURS.toSeconds(time.getHour());
            var minutesInSec = TimeUnit.MINUTES.toSeconds(time.getMinute());
            var toClamp = ((int) Math.floor((hoursInSec + minutesInSec + time.getSecond()) / 3.6) + 24000 - 6000) % 24000;
            return Math.max(0, toClamp);
        }

        if (getType() == TimeSourceType.ASTRO_SUNRISE) {
            // https://zh.wikipedia.org/wiki/%E6%97%A5%E5%87%BA%E6%96%B9%E7%A8%8B%E5%BC%8F
            var localNow = LocalDateTime.now();
            double jDate = localNow.getLong(JulianFields.JULIAN_DAY);
            double jFraction = ((double) localNow.getLong(ChronoField.NANO_OF_DAY)) / Duration.ofDays(1).toNanos();
            jDate += jFraction;

            var nStar = jDate - 2451545 - 0.009 - (latitude / 360);
            var n = new BigDecimal(nStar).setScale(0, RoundingMode.HALF_UP).doubleValue();

            var jStar = 2451545 + 0.0009 + (latitude / 360) + n;

            var m = 357.5291 + 0.98560028 * (jStar - 2451545);
            while (m <= 360) {
                m += 360;
            }
            while (m > 360) {
                m -= 360;
            }

            var c = 1.9148 * Math.sin(m) + 0.02 * Math.sin(m * 2) + 0.0003 * Math.sin(m * 3);

            var lambda = m + 102.9372 + c + 180;
            while (lambda <= 360) {
                lambda += 360;
            }
            while (lambda > 360) {
                lambda -= 360;
            }

            var jTransit = jStar + (0.0053 * Math.sin(m)) - (0.0069 * Math.sin(2 * lambda));

            var delta = Math.asin(Math.sin(lambda) * Math.sin(23.45));

            // Todo.
//            var omega0 = Math.acos()

        }

        throw new RuntimeException("Couldn't fetch time for type " + getType());
    }
}
