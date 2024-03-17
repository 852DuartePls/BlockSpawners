package net.duart.blockspawners.managing;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SpawnerManager {
    private final JavaPlugin plugin;
    private final List<SpawnerBlock> loadedBlocks;

    public SpawnerManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.loadedBlocks = new ArrayList<>();
    }

    public void loadSpawners() {
        File blocksFolder = new File(plugin.getDataFolder(), "blocks");
        if (!blocksFolder.exists()) {
            plugin.getLogger().warning("The 'blocks' folder does not exist.");
            return;
        }

        List<SpawnerBlock> newBlocks = new ArrayList<>();

        for (File file : blocksFolder.listFiles()) {
            if (file.getName().endsWith(".yml") && isLowerCase(file.getName())) {
                String fileName = file.getName().replace(".yml", "").toLowerCase();
                SpawnerBlock existingBlock = findLoadedBlock(fileName);

                long lastModified = file.lastModified();

                if (existingBlock != null) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    String displayName = config.getString("DisplayName");
                    Material material = Material.matchMaterial(config.getString("Material"));
                    List<String> lore = config.getStringList("lore");
                    int spawnTicks = config.getInt("SpawnTicks");
                    List<Material> spawningMaterials = loadSpawningMaterials(config);

                    existingBlock.setDisplayName(displayName);
                    existingBlock.setMaterial(material);
                    existingBlock.setLore(lore);
                    existingBlock.setSpawnTicks(spawnTicks);
                    existingBlock.setSpawningMaterials(spawningMaterials);
                    existingBlock.setLastModified(lastModified);
                    newBlocks.add(existingBlock);
                } else {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    String displayName = config.getString("DisplayName");
                    Material material = Material.matchMaterial(config.getString("Material"));
                    List<String> lore = config.getStringList("lore");
                    int spawnTicks = config.getInt("SpawnTicks");
                    List<Material> spawningMaterials = loadSpawningMaterials(config);

                    SpawnerBlock spawnerBlock = new SpawnerBlock(fileName, displayName, material, lore, spawnTicks, spawningMaterials, plugin);
                    spawnerBlock.setLastModified(lastModified);
                    newBlocks.add(spawnerBlock);
                    plugin.getLogger().info("Spawner registered - " + ChatColor.translateAlternateColorCodes('&', displayName));
                }
            }
        }

        loadedBlocks.clear();
        loadedBlocks.addAll(newBlocks);

        plugin.getLogger().info("BlockSpawners have been reloaded correctly.");
    }


    private SpawnerBlock findLoadedBlock(String fileName) {
        for (SpawnerBlock block : loadedBlocks) {
            if (block.getFileName().equalsIgnoreCase(fileName)) {
                return block;
            }
        }
        return null;
    }

    private boolean isLowerCase(String fileName) {
        return fileName.toLowerCase().equals(fileName);
    }

    private List<Material> loadSpawningMaterials(YamlConfiguration config) {
        List<Material> spawningMaterials = new ArrayList<>();
        if (config.contains("SpawningMaterials")) {
            List<String> spawningMaterialNames = config.getStringList("SpawningMaterials");
            for (String materialName : spawningMaterialNames) {
                Material material = Material.matchMaterial(materialName);
                if (material != null) {
                    spawningMaterials.add(material);
                } else {
                    plugin.getLogger().warning("Unknown material in YAML file: " + materialName);
                }
            }
        } else {
            plugin.getLogger().warning("The YAML file does not contain the 'Spawning Materials' key.");
        }
        return spawningMaterials;
    }

    public List<SpawnerBlock> getLoadedBlocks() {
        return loadedBlocks;
    }
}