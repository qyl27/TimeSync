package cx.rain.mc.timesync.data.model;

import cx.rain.mc.timesync.data.WeatherType;

import java.util.Set;

public class HeFeng implements IWeatherModel {
    public Now now;

    public static class Now {
        public String icon;
    }

    private static final Set<String> SUNNY = Set.of("100", "101", "102", "103", "104", "150", "151", "152", "153");
    private static final Set<String> RAINY = Set.of("300", "301", "302", "305", "306", "309", "313", "314", "350", "399", "400", "401", "402", "405", "406", "407", "408", "456", "499");
    private static final Set<String> THUNDER = Set.of("303", "304", "307", "308", "310", "311", "312", "315", "316", "317", "318", "351", "403", "404", "409", "410");

    public WeatherType toMinecraft() {
        if (RAINY.contains(now.icon)) {
            return WeatherType.RAINY;
        }

        if (THUNDER.contains(now.icon)) {
            return WeatherType.THUNDER;
        }

        return WeatherType.SUNNY;
    }
}
