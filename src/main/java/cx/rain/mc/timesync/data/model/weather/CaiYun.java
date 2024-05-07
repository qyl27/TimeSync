package cx.rain.mc.timesync.data.model.weather;

import cx.rain.mc.timesync.data.WeatherType;

import java.util.Set;

public class CaiYun implements IWeatherModel {
    public Result result;

    public static class Result {
        public RealTime realtime;

        public static class RealTime {
            public String skycon;
        }
    }

    private static final Set<String> SUNNY = Set.of("CLEAR_DAY", "CLEAR_NIGHT", "PARTLY_CLOUDY_DAY", "PARTLY_CLOUDY_NIGHT", "CLOUDY", "LIGHT_HAZE", "MODERATE_HAZE", "HEAVY_HAZE", "FOG");
    private static final Set<String> RAINY = Set.of("LIGHT_RAIN", "MODERATE_RAIN", "LIGHT_SNOW", "MODERATE_SNOW");
    private static final Set<String> THUNDER = Set.of("HEAVY_RAIN", "STORM_RAIN", "HEAVY_SNOW", "STORM_SNOW");

    public WeatherType toMinecraft() {
        if (RAINY.contains(result.realtime.skycon)) {
            return WeatherType.RAINY;
        }

        if (THUNDER.contains(result.realtime.skycon)) {
            return WeatherType.THUNDER;
        }

        return WeatherType.SUNNY;
    }
}
