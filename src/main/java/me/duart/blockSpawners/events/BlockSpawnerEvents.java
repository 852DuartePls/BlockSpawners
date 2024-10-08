package me.duart.blockSpawners.events;

import me.duart.blockSpawners.BlockSpawners;
import me.duart.blockSpawners.manager.LoadBlockSpawners;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BlockSpawnerEvents implements Listener {
    private final BlockSpawners plugin;
    private final LoadBlockSpawners loadBlockSpawners;
    private final Map<Location, ItemStack> placedSpawners = new ConcurrentHashMap<>();
    private final Map<Location, ParticleTasks> particleTasksMap = new ConcurrentHashMap<>();
    private final Map<Location, SpawningTask> spawningTasksMap = new ConcurrentHashMap<>();
    private final File dataFile;
    private final Object fileLock = new Object();

    public BlockSpawnerEvents(BlockSpawners plugin, LoadBlockSpawners loadBlockSpawners) {
        this.plugin = plugin;
        this.loadBlockSpawners = loadBlockSpawners;

        File dataDir = new File(plugin.getDataFolder(), "data");
        if (!dataDir.exists()) {
            boolean dirCreated = dataDir.mkdirs();
            if (!dirCreated) {
                plugin.getLogger().warning("Failed to create the 'data' folder.");
            }
        }

        this.dataFile = new File(dataDir, "spawners.yml");
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

        synchronized (placedSpawners) { originalSpawnerItem = placedSpawners.remove(location); }
        if (originalSpawnerItem == null) return;

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
            synchronized (fileLock) {
                YamlConfiguration config = new YamlConfiguration();

                for (Map.Entry<Location, ItemStack> entry : placedSpawners.entrySet()) {
                    Location loc = entry.getKey();
                    ItemStack item = entry.getValue();
                    String path = loc.getWorld().getName() + "." + loc.getBlockX() + "." + loc.getBlockY() + "." + loc.getBlockZ();

                    config.set(path + ".block", serializeItemStack(item));
                    String itemKey = loadBlockSpawners.getItemKeyFromSpawnerItem(item);
                    if (itemKey != null) {
                        config.set(path + ".itemKey", itemKey);
                    }
                }

                try {
                    config.save(dataFile);
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not save spawners to file: " + e.getMessage());
                }
            }
        });
    }

    private String serializeItemStack(ItemStack item) {
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    public void loadSpawnersFromFile() {
        if (!dataFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);

        for (String world : config.getKeys(false)) {
            if (!config.isConfigurationSection(world)) continue;

            var xSection = config.getConfigurationSection(world);
            if (xSection == null) continue;

            for (String x : xSection.getKeys(false)) {
                if (!xSection.isConfigurationSection(x)) continue;
                var ySection = xSection.getConfigurationSection(x);
                if (ySection == null) continue;

                for (String y : ySection.getKeys(false)) {
                    if (!ySection.isConfigurationSection(y)) continue;
                    var zSection = ySection.getConfigurationSection(y);
                    if (zSection == null) continue;

                    for (String z : zSection.getKeys(false)) {
                        String key = world + "." + x + "." + y + "." + z;
                        String itemData = zSection.getString(z + ".block");
                        String itemKey = zSection.getString(z + ".itemKey");
                        Location location = deserializeLocation(key);
                        if (location != null && itemData != null && itemKey != null) {
                            ItemStack item = deserializeItemStack(itemData);
                            placedSpawners.put(location, item);
                            ParticleTasks particleTasks = new ParticleTasks(location);
                            particleTasks.particleStart();
                            particleTasksMap.put(location, particleTasks);
                            SpawningTask spawningTask = new SpawningTask(loadBlockSpawners, location.getBlock(), itemKey);
                            spawningTask.startTask();
                            spawningTasksMap.put(location, spawningTask);
                        }
                    }
                }
            }
        }
    }

    private Location deserializeLocation(String path) {
        String[] parts = path.split("\\.");
        if (parts.length != 4) {
            plugin.getLogger().warning("Invalid location data: " + path);
            return null;
        }
        String worldName = parts[0];
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int z = Integer.parseInt(parts[3]);

        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }

    private ItemStack deserializeItemStack(String data) {
        byte[] bytes = Base64.getDecoder().decode(data);
        return ItemStack.deserializeBytes(bytes);
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
