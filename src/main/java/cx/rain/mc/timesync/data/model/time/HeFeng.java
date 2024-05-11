package cx.rain.mc.timesync.data.model.time;

import java.time.OffsetDateTime;

public class HeFeng implements ITimeModel {
    public OffsetDateTime sunrise;

    public OffsetDateTime sunset;

    @Override
    public int toMinecraft() {
        var localNow = OffsetDateTime.now();

        if (sunrise == null || sunset == null) {
            var sec = localNow.getHour() * 3600 + localNow.getMinute() * 60 + localNow.getSecond();
            var toClamp = ((int) Math.floor(sec / 3.6) + 24000 - 6000) % 24000;
            return Math.max(0, toClamp);
        }

        var nowSec = localNow.getHour() * 3600 + localNow.getMinute() * 60 + localNow.getSecond();
        var sunriseSec = sunrise.getHour() * 3600 + sunrise.getMinute() * 60 + sunrise.getSecond();
        var sunsetSec = sunset.getHour() * 3600 + sunset.getMinute() * 60 + sunset.getSecond();

        if (localNow.isBefore(sunrise)) {
            // 18000 ~ 22200 (0 ~ 4200)
            var tick = (4200.0 / sunriseSec) * nowSec;
            return ((int) tick) + 18000;
        } else if (localNow.isAfter(sunrise) && localNow.isBefore(sunset)) {
            // 22201 ~ 11616 (0 ~ 13416)
            var tick = (13416.0 / (sunsetSec - sunriseSec)) * (nowSec - sunriseSec);
            return (((int) tick) + 22201) % 24000;
        } else {
            // 11617 ~ 18000 (0 ~ 6383)
            var tick = (6383.0 / (86400 - sunsetSec)) * (nowSec - sunsetSec);
            return ((int) tick) + 11617;
        }
    }
}
