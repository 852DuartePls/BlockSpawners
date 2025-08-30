package me.duart.blockSpawners.events;

import me.duart.blockSpawners.BlockSpawners;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ParticleTasks extends BukkitRunnable {

    // TODO: Make these configurable via yaml files
    private static final Particle.DustOptions ORANGE = new Particle.DustOptions(Color.ORANGE, 1);
    private static final Particle.DustOptions LIME   = new Particle.DustOptions(Color.LIME, 1);
    private static final Particle.DustOptions AQUA   = new Particle.DustOptions(Color.AQUA, 1);
    private static final Particle.DustOptions PURPLE = new Particle.DustOptions(Color.PURPLE, 1);
    private static final int TICKS_PER_SECOND = 60;

    private final Location location;

    public ParticleTasks(Location location) {
        this.location = location.clone().add(0.5, 0.5, 0.5);
    }

    @Override
    public void run() {
        World world = location.getWorld();
        if (world == null) return;
        // TODO: Same thing, configurable amount and colors and toggleable
        world.spawnParticle(Particle.DUST, location, 10, 0.5, 0.5, 0.5, ORANGE);
        world.spawnParticle(Particle.DUST, location, 10, 0.5, 0.5, 0.5, LIME);
        world.spawnParticle(Particle.DUST, location, 10, 0.5, 0.5, 0.5, AQUA);
        world.spawnParticle(Particle.DUST, location, 10, 0.5, 0.5, 0.5, PURPLE);
    }

    public void particleStart() {
        runTaskTimer(BlockSpawners.getPlugin(BlockSpawners.class), 0, TICKS_PER_SECOND);
    }

    public void particleStop() {
        cancel();
    }
}
