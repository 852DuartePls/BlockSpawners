package net.duart.blockspawners.events;

import net.duart.blockspawners.managing.SpawnerBlock;
import net.duart.blockspawners.managing.SpawnerManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockSpawnerEvents implements Listener {
    private final JavaPlugin plugin;
    private final SpawnerManager spawnerManager;
    private final Map<Location, ParticleTask> particleTasks;
    private final Map<Location, SpawningTask> spawningTasks;

    public BlockSpawnerEvents(JavaPlugin plugin, SpawnerManager spawnerManager) {
        this.plugin = plugin;
        this.spawnerManager = spawnerManager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin); // Registra los eventos en el plugin
        this.particleTasks = new HashMap<>();
        this.spawningTasks = new HashMap<>();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        ItemStack item = event.getItemInHand();
        if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null && itemMeta.hasDisplayName()) {
                String displayName = itemMeta.getDisplayName();
                List<String> lore = itemMeta.getLore();
                SpawnerBlock spawnerBlock = getSpawnerBlockByDisplayName(displayName);
                if (spawnerBlock != null) {
                    Location blockLocation = event.getBlock().getLocation();
                    ParticleTask particleTask = new ParticleTask(blockLocation); // Crear una nueva instancia de ParticleTask
                    particleTask.particleStart(); // Iniciar la tarea de partículas
                    particleTasks.put(blockLocation, particleTask);

                    // Start the spawning task if spawnTicks is greater than 0
                    int spawnTicks = spawnerBlock.getSpawnTicks();
                    if (spawnTicks > 0) {
                        SpawningTask spawningTask = new SpawningTask(spawnerBlock, event.getBlock()); // Crear una nueva instancia de SpawningTask
                        spawningTask.startTask(); // Iniciar la tarea de spawneo
                        spawningTasks.put(blockLocation, spawningTask);
                    }

                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getBlock();
        Location location = block.getLocation();

        if (particleTasks.containsKey(location)) {
            ParticleTask particleTask = particleTasks.remove(location);
            if (particleTask != null) {
                particleTask.particleStop();
            }

            SpawningTask spawningTask = spawningTasks.remove(location);
            if (spawningTask != null) {
                spawningTask.cancel(); // Cancelar la tarea de spawneo al romper el bloque
            }

            Player player = event.getPlayer();
            if (player.getGameMode() == GameMode.SURVIVAL) {
                // Droppear el item al romper el bloque solo si el jugador está en Survival
                SpawnerBlock spawnerBlock = getSpawnerBlockByLocation(location);
                if (spawnerBlock != null) {
                    ItemStack spawnerItem = new ItemStack(block.getType());
                    ItemMeta itemMeta = spawnerItem.getItemMeta();
                    if (itemMeta != null) {
                        itemMeta.setDisplayName(spawnerBlock.getFormattedDisplayName());
                        itemMeta.setLore(spawnerBlock.getFormattedLore());
                        itemMeta.addEnchant(Enchantment.DURABILITY, 10, true);
                        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        spawnerItem.setItemMeta(itemMeta);
                        // Hacer el nombre del bloque visible en el suelo
                        Item droppedItem = player.getWorld().dropItemNaturally(location.add(0.5, 0.5, 0.5), spawnerItem);
                        droppedItem.setCustomName(spawnerBlock.getFormattedDisplayName());
                        droppedItem.setCustomNameVisible(true);
                        droppedItem.setGlowing(true); // Hacer que el item drop brille
                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }

    private SpawnerBlock getSpawnerBlockByDisplayName(String displayName) {
        return spawnerManager.getLoadedBlocks().stream()
                .filter(block -> block.getFormattedDisplayName().equals(displayName))
                .findFirst()
                .orElse(null);
    }

    private SpawnerBlock getSpawnerBlockByLocation(Location location) {
        return spawnerManager.getLoadedBlocks().stream()
                .filter(block -> block.getMaterial() == location.getBlock().getType())
                .findFirst()
                .orElse(null);
    }


    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (item != null && item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null && itemMeta.hasDisplayName()) {
                String displayName = itemMeta.getDisplayName();
                List<String> lore = itemMeta.getLore();
                SpawnerBlock spawnerBlock = getSpawnerBlockByDisplayName(displayName);
                if (spawnerBlock != null) {
                    ItemMeta updatedItemMeta = item.getItemMeta();
                    if (updatedItemMeta != null) {
                        updatedItemMeta.setLore(spawnerBlock.getFormattedLore());
                        item.setItemMeta(updatedItemMeta);
                        // Hacer el nombre del bloque visible en el suelo
                        event.getItemDrop().setCustomName(spawnerBlock.getFormattedDisplayName());
                        event.getItemDrop().setCustomNameVisible(true);
                    }
                    event.getItemDrop().setGlowing(true);
                }
            }
        }
    }
}