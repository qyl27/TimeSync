package cx.rain.mc.timesync.task;

import org.bukkit.GameRule;
import org.bukkit.World;

public class WorldTimeFliesTask implements Runnable {
    private final World world;

    public WorldTimeFliesTask(World world) {
        this.world = world;
    }

    @Override
    public void run() {
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setTime(world.getTime() + 1);
    }
}
