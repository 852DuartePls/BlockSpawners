package net.duart.blockspawners.events;

import net.duart.blockspawners.BlockSpawners;
import net.duart.blockspawners.managing.SpawnerBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class SpawningTask extends BukkitRunnable {
    private final SpawnerBlock spawnerBlock;
    private final Block spawnerBlockLocation;
    private final Random random;

    public SpawningTask(SpawnerBlock spawnerBlock, Block spawnerBlockLocation) {
        this.spawnerBlock = spawnerBlock;
        this.spawnerBlockLocation = spawnerBlockLocation;
        this.random = new Random();
    }

    @Override
    public void run() {
        if (spawnerBlockLocation.getType() != spawnerBlock.getMaterial()) {
            this.cancel();
            BlockSpawners.getPlugin(BlockSpawners.class).getLogger().log(Level.INFO, "[SpawningTask] - Custom block is no longer the correct type. Task canceled.");
            return;
        }

        List<Material> spawningMaterials = spawnerBlock.getSpawningMaterials();
        if (spawningMaterials.isEmpty()) {
            BlockSpawners.getPlugin(BlockSpawners.class).getLogger().log(Level.INFO, "[SpawningTask] - Spawn materials list is empty.");
            return;
        }

        Material randomMaterial = spawningMaterials.get(random.nextInt(spawningMaterials.size()));
        ItemStack itemStack = new ItemStack(randomMaterial);
        Location dropLocation = spawnerBlockLocation.getLocation().add(0.5, 0.5, 0.5); // Colocar el item en el centro superior del bloque
        Item spawnedItem = dropLocation.getWorld().dropItemNaturally(dropLocation, itemStack);
        spawnedItem.setCustomName(spawnerBlock.getFormattedDisplayName());
        spawnedItem.setCustomNameVisible(false);
        if (spawnedItem == null) {
            BlockSpawners.getPlugin(BlockSpawners.class).getLogger().log(Level.WARNING, "[SpawningTask] - Could not spawn item.");
        }
    }

    public void startTask() {
        int spawnTicks = spawnerBlock.getSpawnTicks();
        this.runTaskTimer(BlockSpawners.getPlugin(BlockSpawners.class), spawnTicks, spawnTicks);
    }
}