package me.duart.blockSpawners.manager;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static me.duart.blockSpawners.BlockSpawners.mini;

public class LoadBlockSpawners {
    private final JavaPlugin plugin;
    private final Map<String, ItemStack> items;
    private final File spawnersFolder;
    private final Map<String, List<Material>> spawningMaterialsMap = new HashMap<>();
    private final Map<String, Integer> spawnTicksMap = new HashMap<>();

    public LoadBlockSpawners(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.items = new HashMap<>();
        this.spawnersFolder = new File(plugin.getDataFolder(), "spawners");
        setupSpawnerFiles();
    }

    public CompletableFuture<Boolean> reloadSpawnerFiles() {
        return CompletableFuture.supplyAsync(() -> {
            items.clear();
            spawningMaterialsMap.clear();

            File[] spawnerFiles = spawnersFolder.listFiles((dir, name) -> name.endsWith(".yml"));

            if (spawnerFiles == null || spawnerFiles.length == 0) {
                plugin.getLogger().warning("No spawner files found in the spawners folder.");
                return false;
            }

            boolean allFilesLoadedSuccessfully = true;

            for (File spawnerFile : spawnerFiles) {
                try {
                    loadItemFromFile(spawnerFile);
                } catch (Exception e) {
                    plugin.getLogger().severe("Error loading spawner file: " + spawnerFile.getName() + " - " + e.getMessage());
                    allFilesLoadedSuccessfully = false;
                }
            }

            if (allFilesLoadedSuccessfully) {
                plugin.getLogger().info("Spawner files reloaded successfully.");
            } else {
                plugin.getLogger().warning("Some spawner files failed to load.");
            }

            return allFilesLoadedSuccessfully;
        });
    }

    public void setupSpawnerFiles() {
        if (!spawnersFolder.exists()) {
            try {
                Files.createDirectories(spawnersFolder.toPath());
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create folder: " + spawnersFolder.getAbsolutePath());
            }
        }
        String[] spawnerFiles = {
                "concretespawner.yml", "stainedglassspawner.yml", "woolspawner.yml",
                "woodspawner.yml", "clayspawner.yml", "concretespawner.yml",
                "dirtspawner.yml", "glassspawner.yml", "glazedspawner.yml",
                "glowstonespawner.yml", "granitespawner.yml", "grassspawner.yml",
                "myceliumspawner.yml", "netherrackspawner.yml", "obsidianspawner",
                "prismarinespawner.yml", "quartzspawner.yml", "sandspawner.yml",
                "sealanternspawner.yml", "terracottaspawner.yml"
        };

        for (String fileName : spawnerFiles) {
            File spawnerFile = new File(spawnersFolder, fileName);
            if (!spawnerFile.exists()) {
                try (InputStream inputStream = plugin.getResource(fileName)) {
                    if (inputStream == null) {
                        continue;
                    }

                    Files.copy(inputStream, spawnerFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    plugin.getLogger().warning("Error while copying file " + fileName + ": " + e.getMessage());
                }
            }
            loadItemFromFile(spawnerFile);
        }
    }

    public boolean isSpawnerItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return true;
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return true;

        for (String key : items.keySet()) {
            NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
            if (itemMeta.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING)) {
                String value = itemMeta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
                if (value != null && value.equals(key)) return false;
            }
        }
        return true;
    }

    public ItemStack getItem(String key) {
        return items.get(key);
    }

    public List<String> getItemKeys() {
        return new ArrayList<>(items.keySet());
    }

    public String getItemKeyFromSpawnerItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            return null;
        }

        for (String key : items.keySet()) {
            NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
            if (itemMeta.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING)) {
                String value = itemMeta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
                if (value != null && value.equals(key)) {
                    return key;
                }
            }
        }
        return null;
    }

    public Component getDisplayName(String key) {
        ItemStack itemStack = getItem(key);
        if (itemStack != null && itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null && itemMeta.hasDisplayName()) {
                return itemMeta.displayName();
            }
        }
        return null;
    }

    public int getSpawnTicksForItem(String itemKey) {
        return spawnTicksMap.getOrDefault(itemKey, 200);
    }

    public List<Material> getSpawningMaterialsForItem(String itemKey) {
        return spawningMaterialsMap.getOrDefault(itemKey, new ArrayList<>());
    }

    private void loadItemFromFile(@NotNull File file) {
        String itemKey = file.getName().toLowerCase().replace(".yml", "");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection itemConfig = null;
        for (String key : config.getKeys(false)) {
            if (key.equalsIgnoreCase(itemKey)) {
                itemConfig = config.getConfigurationSection(key);
                break;
            }
        }

        if (itemConfig == null) {
            plugin.getLogger().warning("Item configuration section not found for: " + itemKey);
            return;
        }

        int spawnTicksValue = itemConfig.getInt("SpawnTicks", 200);
        spawnTicksMap.put(itemKey, spawnTicksValue);

        List<Material> materials = new ArrayList<>();
        List<String> materialNames = itemConfig.getStringList("SpawningMaterials");
        for (String materialName : materialNames) {
            Material material = Material.matchMaterial(materialName.toUpperCase());
            if (material != null) materials.add(material);
            else plugin.getLogger().warning("Invalid material: " + materialName);
        }
        spawningMaterialsMap.put(itemKey, materials);

        Material material = Material.matchMaterial(itemConfig.getString("Material", "STONE").toUpperCase());
        if (material == null || !material.isBlock()) {
            plugin.getLogger().warning("Invalid material for item " + itemKey + ": " + itemConfig.getString("Material"));
            return;
        }

        if (material.hasGravity()) {
            plugin.getLogger().warning("\n"
                    + "============================================\n"
                    + "WARNING: The block set for item \"" + itemKey + "\" has gravity!\n"
                    + "Material: " + material.name() + "\n"
                    + "This could lead to misbehavior when placed in the world,\n"
                    + "such as falling or breaking unexpectedly.\n"
                    + "Please consider using a block that is not affected by gravity.\n"
                    + "============================================"
            );
        }

        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            plugin.getLogger().warning("Could not get ItemMeta for: " + itemKey);
            return;
        }

        String displayName = itemConfig.getString("DisplayName");
        if (displayName != null) {
            Component componentDisplayName = mini.deserialize(displayName).decoration(TextDecoration.ITALIC, false);
            itemMeta.displayName(componentDisplayName);
        }

        List<Component> lore = itemConfig.getStringList("Lore").stream()
                .map(loreLine -> mini.deserialize(loreLine).decoration(TextDecoration.ITALIC, false))
                .collect(Collectors.toList());

        if (!lore.isEmpty()) {
            itemMeta.lore(lore);
        }

        ConfigurationSection enchantmentsConfig = itemConfig.getConfigurationSection("Enchantments");
        if (enchantmentsConfig != null) {
            RegistryAccess registryAccess = RegistryAccess.registryAccess();
            for (String enchantmentKey : enchantmentsConfig.getKeys(false)) {
                String lowercaseEnchantmentKey = enchantmentKey.toLowerCase();
                Enchantment enchantment = registryAccess.getRegistry(RegistryKey.ENCHANTMENT).get(NamespacedKey.minecraft(lowercaseEnchantmentKey));
                if (enchantment != null) {
                    itemMeta.addEnchant(enchantment, enchantmentsConfig.getInt(enchantmentKey), true);
                } else {
                    plugin.getLogger().warning("Enchantment " + enchantmentKey + " not found in config!");
                }
            }
        }

        boolean unbreakable = itemConfig.getBoolean("Unbreakable", true);
        ConfigurationSection flagsSection = itemConfig.getConfigurationSection("Flags");
        boolean hideEnchantments = flagsSection != null && flagsSection.getBoolean("HideEnchantments", false);
        boolean hideUnbreakable = flagsSection != null && flagsSection.getBoolean("HideUnbreakable", false);

        itemMeta.setUnbreakable(unbreakable);
        if (hideEnchantments) itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        if (hideUnbreakable) itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        itemMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, itemKey), PersistentDataType.STRING, itemKey);
        itemStack.setItemMeta(itemMeta);

        items.put(itemKey, itemStack);
        plugin.getLogger().info("Loaded item: " + itemKey);
    }
}
