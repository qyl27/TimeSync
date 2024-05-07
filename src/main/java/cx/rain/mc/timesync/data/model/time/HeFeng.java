package cx.rain.mc.timesync.data.model.time;

import java.time.ZonedDateTime;

public class HeFeng implements ITimeModel {
    public ZonedDateTime sunrise;

    public ZonedDateTime sunset;

    @Override
    public int toMinecraft() {
        var localNow = ZonedDateTime.now();

        var nowSec = localNow.getHour() * 3600 + localNow.getMinute() * 60 + localNow.getSecond();
        var sunriseSec = sunrise.getHour() * 3600 + sunrise.getMinute() * 60 + sunrise.getSecond();
        var sunsetSec = sunset.getHour() * 3600 + sunset.getMinute() * 60 + sunset.getSecond();

        if (localNow.isBefore(sunrise)) {
            // 18000 ~ 22200 (0 ~ 4200)
            var tick = (sunriseSec / 4200.0) * nowSec;
            return ((int) tick) + 18000;
        } else if (localNow.isAfter(sunrise) && localNow.isBefore(sunset)) {
            // 22201 ~ 11616 (0 ~ 13416)
            var tick = ((sunsetSec - sunriseSec) / 13416.0) * (nowSec - sunriseSec);
            return (((int) tick) + 22201) % 24000;
        } else {
            // 11617 ~ 18000 (0 ~ 6383)
            var tick = ((86400 - sunsetSec) / 6383.0) * (nowSec - sunsetSec);
            return ((int) tick) + 11617;
        }
    }
}
