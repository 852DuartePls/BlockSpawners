package me.duart.blockSpawners.events;

import me.duart.blockSpawners.BlockSpawners;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ParticleTasks extends BukkitRunnable {

    private static final int TICKS_PER_SECOND = 60;
    private final Location location;

    public ParticleTasks(Location location) {
        this.location = location.clone().add(0.5, 0.5, 0.5);
    }

    @Override
    public void run() {
        World world = location.getWorld();
        if (world == null) return;
        world.spawnParticle(Particle.DUST, location, 10, 0.5, 0.5, 0.5,
                new Particle.DustOptions(Color.fromRGB(255, 140, 0), 1));
        world.spawnParticle(Particle.DUST, location, 10, 0.5, 0.5, 0.5,
                new Particle.DustOptions(Color.fromRGB(127, 255, 0), 1));
        world.spawnParticle(Particle.DUST, location, 10, 0.5, 0.5, 0.5,
                new Particle.DustOptions(Color.fromRGB(0, 191, 255), 1));
        world.spawnParticle(Particle.DUST, location, 10, 0.5, 0.5, 0.5,
                new Particle.DustOptions(Color.fromRGB(138, 43, 226), 1));
    }

    public void particleStart() {
        runTaskTimer(JavaPlugin.getPlugin(BlockSpawners.class), 0, TICKS_PER_SECOND);
    }

    public void particleStop() {
        cancel();
    }
}
