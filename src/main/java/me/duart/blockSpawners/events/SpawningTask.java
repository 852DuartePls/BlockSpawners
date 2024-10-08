package me.duart.blockSpawners.events;

import me.duart.blockSpawners.BlockSpawners;
import me.duart.blockSpawners.manager.LoadBlockSpawners;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class SpawningTask  extends BukkitRunnable {
    private final LoadBlockSpawners loadBlockSpawners;
    private final Block spawnerBlockLocation;
    private final Random random;
    private final String itemKey;
    private static final Logger LOGGER = BlockSpawners.getPlugin(BlockSpawners.class).getLogger();
    private static final Material DEFAULT_SPAWN_MATERIAL = Material.RED_WOOL;

    public SpawningTask(LoadBlockSpawners loadBlockSpawners, Block spawnerBlockLocation, String itemKey) {
        this.loadBlockSpawners = loadBlockSpawners;
        this.spawnerBlockLocation = spawnerBlockLocation;
        this.random = new Random();
        this.itemKey = itemKey;
    }

    @Override
    public void run() {
        if (spawnerBlockLocation == null) return;

        List<Material> spawningMaterials = loadBlockSpawners.getSpawningMaterialsForItem(itemKey);
        if (spawningMaterials.isEmpty()) {
            LOGGER.warning("[SpawningTask] - Spawn materials list is empty, defaulting to RED_WOOL");
            spawnerBlockLocation.getWorld().dropItemNaturally(spawnerBlockLocation.getLocation(), new ItemStack(DEFAULT_SPAWN_MATERIAL));
            return;
        }

        Material randomMaterial = spawningMaterials.get(random.nextInt(spawningMaterials.size()));
        ItemStack itemStack = new ItemStack(randomMaterial);
        Location dropLocation = spawnerBlockLocation.getLocation().add(0,0,0);
        Item spawnedItem = spawnerBlockLocation.getWorld().dropItemNaturally(dropLocation, itemStack);
        spawnedItem.setCustomNameVisible(false);
    }

    public void startTask() {
        int spawnTicks = loadBlockSpawners.getSpawnTicksForItem(itemKey);
        this.runTaskTimer(BlockSpawners.getPlugin(BlockSpawners.class), spawnTicks, spawnTicks);
    }

    public void stopTask() {
        this.cancel();
    }
}
