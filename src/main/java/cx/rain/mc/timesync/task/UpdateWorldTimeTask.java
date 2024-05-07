package cx.rain.mc.timesync.task;

import cx.rain.mc.timesync.data.TimeSource;
import cx.rain.mc.timesync.data.WeatherType;
import org.bukkit.GameRule;
import org.bukkit.World;

public class UpdateWorldTimeTask implements Runnable {
    private final World world;
    private final TimeSource source;

    public UpdateWorldTimeTask(World world, TimeSource source) {
        this.world = world;
        this.source = source;
    }

    @Override
    public void run() {
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        try {
            var tick = source.fetch();
            world.setTime(tick);
            System.out.println(tick);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
