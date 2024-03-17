package net.duart.blockspawners.events;

import net.duart.blockspawners.BlockSpawners;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleTask extends BukkitRunnable {
    private static final int TICK_INTERVAL = 20; // 1 second
    private final Location location;

    public ParticleTask(Location location) {
        this.location = location.clone().add(0.5, 0.5, 0.5); // Center particles on the block
    }

    @Override
    public void run() {
        World world = location.getWorld();
        if (world != null) {
            world.spawnParticle(Particle.REDSTONE, location, 10, 0.5, 0.5, 0.5,
                    new Particle.DustOptions(Color.fromRGB(255, 140, 0), 1)); // Dark Orange
            world.spawnParticle(Particle.REDSTONE, location, 10, 0.5, 0.5, 0.5,
                    new Particle.DustOptions(Color.fromRGB(127, 255, 0), 1)); // Spring Green
            world.spawnParticle(Particle.REDSTONE, location, 10, 0.5, 0.5, 0.5,
                    new Particle.DustOptions(Color.fromRGB(0, 191, 255), 1)); // Deep Sky Blue
            world.spawnParticle(Particle.REDSTONE, location, 10, 0.5, 0.5, 0.5,
                    new Particle.DustOptions(Color.fromRGB(138, 43, 226), 1)); // Blue Violet
        }
    }
    public void particleStart() {
        runTaskTimer(JavaPlugin.getPlugin(BlockSpawners.class), 0, TICK_INTERVAL);
    }

    public void particleStop() {
        cancel();
    }
}

