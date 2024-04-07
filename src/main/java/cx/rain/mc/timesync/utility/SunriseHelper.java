package cx.rain.mc.timesync.utility;

import java.time.ZonedDateTime;

public class SunriseHelper {
    public static boolean isLeapYear(int year) {
        return (year % 400 == 0) || ((year % 4 == 0) && (year % 100 != 0));
    }

    public static double sunriseHA(ZonedDateTime time, double latitude) {
        var dec = -Math.toRadians(23.44) * Math.cos(2 * Math.PI / (365 + (isLeapYear(time.getYear()) ? 1 : 0)) * (time.getDayOfYear() + 10));
        var co = -Math.tan(Math.toRadians(latitude)) * Math.tan(dec);
        return Math.acos(co);
    }

    public static double getSunriseSecond(double ha) {
        while (ha > Math.PI) {
            ha -= Math.PI * 2;
        }

        if (ha > 0) {
            ha = -ha;
        }

        return 43200 + (ha * 43200 / Math.PI);
    }

    public static double getSunsetSecond(double ha) {
        while (ha > Math.PI) {
            ha -= Math.PI * 2;
        }

        if (ha < 0) {
            ha = -ha;
        }

        return 43200 + (ha * 43200 / Math.PI);
    }
}
