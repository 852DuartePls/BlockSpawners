package me.duart.blockSpawners.events;

import me.duart.blockSpawners.BlockSpawners;
import me.duart.blockSpawners.manager.DataStorage;
import me.duart.blockSpawners.manager.LoadBlockSpawners;
import me.duart.blockSpawners.manager.SQLiteDataStorage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static me.duart.blockSpawners.BlockSpawners.mini;

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

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerPlaceSpawnerOrPowder(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack spawnerItem = event.getItemInHand();

        String itemKey = loadBlockSpawners.getItemKeyFromSpawnerItem(spawnerItem);
        if (itemKey == null) return;

        Block block = event.getBlock();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        boolean isConcretePowder = mainHandItem.getType().toString().contains("_CONCRETE_POWDER") ||
                offHandItem.getType().toString().contains("_CONCRETE_POWDER");

        if (isConcretePowder) {
            String formattedItemKey = formatItemKey(itemKey);
            for (BlockFace face : BlockFace.values()) {
                Block adjacentBlock = block.getRelative(face);
                if (adjacentBlock.getType() == Material.WATER) {
                    event.setCancelled(true);
                    player.sendMessage(mini.deserialize((
                            "<newline><color:#ff2e38>You can't place this <color:#ff8112>" + formattedItemKey +
                                    "</color> next to water! Please avoid this since it is a <color:#f4ff21>Concrete Powder</color> " +
                                    "and it can interfere with the correct behaviour of the spawner.</color><newline>"
                    )));
                    return;
                }
            }
        }

        if (loadBlockSpawners.isSpawnerItem(spawnerItem)) return;

        Location location = block.getLocation();
        ParticleTasks particleTasks = new ParticleTasks(location);
        particleTasks.particleStart();
        particleTasksMap.put(location, particleTasks);
        placedSpawners.put(location, spawnerItem.clone());

        int spawnTicks = loadBlockSpawners.getSpawnTicksForItem(itemKey);
        if (spawnTicks <= 0) return;

        SpawningTask spawningTask = new SpawningTask(loadBlockSpawners, block, itemKey);
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
        ItemStack originalSpawnerItem = placedSpawners.get(location);
        if (event.isCancelled()) return;
        if (particleTasks != null) particleTasks.particleStop();
        if (spawningTask != null) spawningTask.stopTask();

        if (originalSpawnerItem == null) return;

        CompletableFuture.runAsync(() -> {
            try {
                dataStorage.removeSpawnerData(location);
            } catch (SQLException e) {
                plugin.getLogger().severe("Error removing spawner data: " + e.getMessage());
            }
        });

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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        Block movedBlock = event.getBlock().getRelative(event.getDirection());
        if (!placedSpawners.containsKey(movedBlock.getLocation())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (placedSpawners.containsKey(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBurn(BlockBurnEvent event) {
        Block burnedBlock = event.getBlock();
        if (!placedSpawners.containsKey(burnedBlock.getLocation())) return;

        event.setCancelled(true);

        for (BlockFace face : BlockFace.values()) {
            Block adjacentBlock = burnedBlock.getRelative(face);
            if (adjacentBlock.getType() == Material.FIRE) {
                adjacentBlock.setType(Material.AIR);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockExplode(BlockExplodeEvent event) {
        Block explodedBlock = event.getBlock();
        if (!placedSpawners.containsKey(explodedBlock.getLocation())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> placedSpawners.containsKey(block.getLocation()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFallingBlock(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof FallingBlock)) return;

        Location location = event.getBlock().getLocation();
        if (!placedSpawners.containsKey(location)) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onConcretePowderForm(BlockFormEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();

        if (!placedSpawners.containsKey(location)) return;

        String blockMaterial = block.getType().toString();
        String newStateMaterial = event.getNewState().getType().toString();

        if (!blockMaterial.contains("_CONCRETE_POWDER")) return;
        if (!newStateMaterial.contains("_CONCRETE")) return;

        event.setCancelled(true);
    }

    private final ExecutorService saverExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "BlockSpawners-Saver");
        thread.setDaemon(true);
        return thread;
    });

    private void saveSpawnersToFile() {
        CompletableFuture.runAsync(() -> {
            try {
                dataStorage.saveSpawnerDataAsync(placedSpawners).get();
            } catch (Exception e) {
                plugin.getLogger().severe("Error saving spawner data: " + e.getMessage());
            }
        }, saverExecutor);
    }

    public void loadSpawnersFromFile() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return dataStorage.loadSpawnerDataAsync().get();
            } catch (Exception e) {
                plugin.getLogger().severe("Error loading spawner data: " + e.getMessage());
                return new HashMap<Location, ItemStack>();
            }
        }).thenAccept(spawners -> {
            placedSpawners.putAll(spawners);
            for (Map.Entry<Location, ItemStack> entry : placedSpawners.entrySet()) {
                Location location = entry.getKey();
                ItemStack item = entry.getValue();
                Block block = location.getBlock();

                if (isInvalidBlock(block)) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            dataStorage.removeSpawnerData(location);
                        } catch (SQLException e) {
                            plugin.getLogger().severe("Removing spawner tasks from " + location + " because the block is invalid \n" + e.getMessage());
                        }
                    });
                } else {
                    ParticleTasks particleTasks = new ParticleTasks(location);
                    particleTasks.particleStart();
                    particleTasksMap.put(location, particleTasks);
                    String itemKey = loadBlockSpawners.getItemKeyFromSpawnerItem(item);
                    if (itemKey != null) {
                        SpawningTask spawningTask = new SpawningTask(loadBlockSpawners, block, itemKey);
                        spawningTask.startTask();
                        spawningTasksMap.put(location, spawningTask);
                    }
                }
            }
        });
    }

    private boolean isInvalidBlock(Block block) {
        Material type = block.getType();
        return type == Material.AIR || type == Material.WATER || type == Material.LAVA;
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

    private String formatItemKey(String itemKey) {
        if (itemKey.endsWith("spawner")) {
            String base = itemKey.substring(0, itemKey.length() - 7);
            return capitalizeFirstLetter(base) + " Spawner";
        }
        return itemKey;
    }

    private String capitalizeFirstLetter(String str) {
        if (str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
    }
}
