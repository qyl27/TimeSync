package cx.rain.mc.timesync.task;

import cx.rain.mc.timesync.data.WeatherSource;
import cx.rain.mc.timesync.data.WeatherType;
import org.bukkit.GameRule;
import org.bukkit.World;

public class UpdateWorldWeatherTask implements Runnable {
    private final World world;
    private final WeatherSource source;

    public UpdateWorldWeatherTask(World world, WeatherSource source) {
        this.world = world;
        this.source = source;
    }

    @Override
    public void run() {
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);

        try {
            var result = source.fetch();

            world.setWeatherDuration(Integer.MAX_VALUE);
            world.setThunderDuration(Integer.MAX_VALUE);

            if (result == WeatherType.SUNNY) {
                world.setStorm(false);
                world.setThundering(false);
            } else if (result == WeatherType.RAINY) {
                world.setStorm(true);
                world.setThundering(false);
            } else {
                world.setStorm(false);
                world.setThundering(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
