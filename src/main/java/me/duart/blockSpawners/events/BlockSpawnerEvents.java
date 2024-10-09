package me.duart.blockSpawners.events;

import me.duart.blockSpawners.BlockSpawners;
import me.duart.blockSpawners.manager.DataStorage;
import me.duart.blockSpawners.manager.LoadBlockSpawners;
import me.duart.blockSpawners.manager.SQLiteDataStorage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public class BlockSpawnerEvents implements Listener {
    private final BlockSpawners plugin;
    private final LoadBlockSpawners loadBlockSpawners;
    private final Map<Location, ItemStack> placedSpawners = new ConcurrentHashMap<>();
    private final Map<Location, ParticleTasks> particleTasksMap = new ConcurrentHashMap<>();
    private final Map<Location, SpawningTask> spawningTasksMap = new ConcurrentHashMap<>();
    private final DataStorage dataStorage;

    public BlockSpawnerEvents(BlockSpawners plugin, LoadBlockSpawners loadBlockSpawners) {
        this.plugin = plugin;
        this.loadBlockSpawners = loadBlockSpawners;
        this.dataStorage = new SQLiteDataStorage(plugin, loadBlockSpawners);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropSpawner(PlayerDropItemEvent event) {
        ItemStack spawnerItem = event.getItemDrop().getItemStack();
        if (loadBlockSpawners.isSpawnerItem(spawnerItem)) return;
        String itemKey = loadBlockSpawners.getItemKeyFromSpawnerItem(spawnerItem);
        if (itemKey == null) return;
        Component displayName = loadBlockSpawners.getDisplayName(itemKey);
        if (displayName == null) return;
        event.getItemDrop().customName(displayName);
        event.getItemDrop().setCustomNameVisible(true);
        event.getItemDrop().setGlowing(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerPlaceSpawner(BlockPlaceEvent event) {
        ItemStack spawnerItem = event.getItemInHand();
        String itemKey = loadBlockSpawners.getItemKeyFromSpawnerItem(spawnerItem);
        if (event.isCancelled()) return;
        if (itemKey == null) return;
        if (loadBlockSpawners.isSpawnerItem(spawnerItem)) return;

        Location location = event.getBlockPlaced().getLocation();
        ParticleTasks particleTasks = new ParticleTasks(location);
        particleTasks.particleStart();
        particleTasksMap.put(location, particleTasks);
        placedSpawners.put(location, spawnerItem.clone());

        int spawnTicks = loadBlockSpawners.getSpawnTicksForItem(itemKey);
        if (spawnTicks <= 0) return;

        SpawningTask spawningTask = new SpawningTask(loadBlockSpawners, event.getBlock(), itemKey);
        spawningTask.startTask();
        spawningTasksMap.put(location, spawningTask);
        saveSpawnersToFile();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBreakSpawner(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        ParticleTasks particleTasks = particleTasksMap.remove(location);
        SpawningTask spawningTask = spawningTasksMap.remove(location);
        ItemStack originalSpawnerItem;
        if (event.isCancelled()) return;
        if (particleTasks != null) particleTasks.particleStop();
        if (spawningTask != null) spawningTask.stopTask();

        synchronized (placedSpawners) {
            originalSpawnerItem = placedSpawners.remove(location);
        }
        if (originalSpawnerItem == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                dataStorage.removeSpawnerData(location);
            } catch (SQLException e) {
                plugin.getLogger().severe("Error removing spawner data: " + e.getMessage());
            }
        });

        saveSpawnersToFile();

        Player player = event.getPlayer();
        ItemStack droppedItemStack = originalSpawnerItem.clone();

        if (player.getGameMode() != GameMode.SURVIVAL) return;
        event.setDropItems(false);
        droppedItemStack.setAmount(1);
        Item droppedItem = player.getWorld().dropItemNaturally(location.add(0.5, 0.5, 0.5), droppedItemStack);
        if (originalSpawnerItem.hasItemMeta() && originalSpawnerItem.getItemMeta().hasDisplayName()) {
            droppedItem.customName(originalSpawnerItem.getItemMeta().displayName());
            droppedItem.setCustomNameVisible(true);
            droppedItem.setGlowing(true);
        }
    }

    private void saveSpawnersToFile() {
        CompletableFuture.runAsync(() -> {
            try {
                dataStorage.saveSpawnerData(placedSpawners);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void loadSpawnersFromFile() {
        CompletableFuture.runAsync(() -> {
            try {
                placedSpawners.putAll(dataStorage.loadSpawnerData());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            for (Map.Entry<Location, ItemStack> entry : placedSpawners.entrySet()) {
                Location location = entry.getKey();
                ItemStack item = entry.getValue();
                ParticleTasks particleTasks = new ParticleTasks(location);
                particleTasks.particleStart();
                particleTasksMap.put(location, particleTasks);
                String itemKey = loadBlockSpawners.getItemKeyFromSpawnerItem(item);
                if (itemKey != null) {
                    SpawningTask spawningTask = new SpawningTask(loadBlockSpawners, location.getBlock(), itemKey);
                    spawningTask.startTask();
                    spawningTasksMap.put(location, spawningTask);
                }
            }
        });
    }

    public CompletableFuture<Void> stopAllTasks() {
        return CompletableFuture.runAsync(() -> {
            for (ParticleTasks particleTask : particleTasksMap.values()) {
                particleTask.particleStop();
            }
            particleTasksMap.clear();
            for (SpawningTask spawningTask : spawningTasksMap.values()) {
                spawningTask.stopTask();
            }
            spawningTasksMap.clear();
        }).thenRun(() -> Bukkit.getScheduler().runTask(plugin, this::loadSpawnersFromFile));
    }
}
