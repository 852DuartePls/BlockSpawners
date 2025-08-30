package me.duart.blockSpawners.events;

import me.duart.blockSpawners.BlockSpawners;
import me.duart.blockSpawners.manager.LoadBlockSpawners;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SpawningTask  extends BukkitRunnable {
    private final LoadBlockSpawners loader;
    private final Block blockLoc;
    private final String itemKey;
    private final List<Material> materials;
    private final ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

    public SpawningTask(@NotNull LoadBlockSpawners loader, Block blockLoc, String itemKey) {
        this.loader = loader;
        this.blockLoc = blockLoc;
        this.itemKey = itemKey;
        this.materials = loader.getSpawningMaterialsForItem(itemKey);
    }

    @Override
    public void run() {
        if (materials.isEmpty()) return;

        Material MaterialType = materials.get(threadLocalRandom.nextInt(materials.size()));
        ItemStack stack = new ItemStack(MaterialType);
        // TODO: Make this configurable -- ↴
        Location drop = blockLoc.getLocation().add(0.5, 1, 0.5);
        Item item = blockLoc.getWorld().dropItemNaturally(drop, stack);
        item.setCustomNameVisible(false);
        // ------------------------------- ⬏
    }

    public void startTask() {
        int spawnTicks = loader.getSpawnTicksForItem(itemKey);
        runTaskTimer(BlockSpawners.getPlugin(BlockSpawners.class), spawnTicks, spawnTicks);
    }

    public void stopTask() {
        this.cancel();
    }
}
