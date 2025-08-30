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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static me.duart.blockSpawners.BlockSpawners.mini;

public class LoadBlockSpawners {
    private final JavaPlugin plugin;
    private final Map<String, ItemStack> items = new HashMap<>();
    private final Map<String, NamespacedKey> keys = new HashMap<>();
    private final File spawnersFolder;
    private final Map<String, List<Material>> spawningMaterialsMap = new HashMap<>();
    private final Map<String, Integer> spawnTicksMap = new HashMap<>();

    public LoadBlockSpawners(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.spawnersFolder = new File(plugin.getDataFolder(), "spawners");
        setupSpawnerFiles();
    }

    public CompletableFuture<Boolean> reloadSpawnerFiles() {
        return CompletableFuture.supplyAsync(() -> {
            items.clear();
            keys.clear();
            spawningMaterialsMap.clear();
            spawnTicksMap.clear();

            File[] spawnerFiles = spawnersFolder.listFiles((dir, name) -> name.endsWith(".yml"));

            if (spawnerFiles == null || spawnerFiles.length == 0) {
                plugin.getLogger().warning("No spawner files found in the spawners folder.");
                return false;
            }

            boolean allFilesLoadedSuccessfully = true;

            for (File file : spawnerFiles) {
                try {
                    loadItemFromFile(file);
                } catch (Exception e) {
                    plugin.getLogger().severe("Error loading " + file.getName() + " - " + e.getMessage());
                    allFilesLoadedSuccessfully = false;
                }
            }
            plugin.getLogger().info("Spawner files reloaded. Success: " + allFilesLoadedSuccessfully);
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

        String[] defaultFiles = {
                "concretespawner.yml", "stainedglassspawner.yml", "woolspawner.yml",
                "woodspawner.yml", "clayspawner.yml", "concretespawner.yml",
                "dirtspawner.yml", "glassspawner.yml", "glazedspawner.yml",
                "glowstonespawner.yml", "granitespawner.yml", "grassspawner.yml",
                "myceliumspawner.yml", "netherrackspawner.yml", "obsidianspawner",
                "prismarinespawner.yml", "quartzspawner.yml", "sandspawner.yml",
                "sealanternspawner.yml", "terracottaspawner.yml"
        };

        for (String fileName : defaultFiles) {
            File targetFile = new File(spawnersFolder, fileName);
            if (!targetFile.exists()) {
                try (InputStream inputStream = plugin.getResource(fileName)) {
                    if (inputStream != null) {
                        Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    plugin.getLogger().warning("Error while copying file " + fileName + ": " + e.getMessage());
                }
            }
            loadItemFromFile(targetFile);
        }
    }

    public boolean isNotSpawnerItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return true;
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return true;

        for (NamespacedKey key : keys.values()) {
            if (itemMeta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) return false;
        }
        return true;
    }

    public String getItemKeyFromSpawnerItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return null;
        for (Map.Entry<String, NamespacedKey> e : keys.entrySet()) {
            String val = itemMeta.getPersistentDataContainer().get(e.getValue(), PersistentDataType.STRING);
            if (val != null) return e.getKey();
        }
        return null;
    }

    public ItemStack getItem(String key) {
        return items.get(key);
    }

    public List<String> getItemKeys() {
        return List.copyOf(items.keySet());
    }

    public Component getDisplayName(String key) {
        ItemStack itemStack = getItem(key);
        return (itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
                ? itemStack.getItemMeta().displayName()
                : null;

    }

    public int getSpawnTicksForItem(String itemKey) {
        return spawnTicksMap.getOrDefault(itemKey, 200);
    }

    public List<Material> getSpawningMaterialsForItem(String itemKey) {
        return spawningMaterialsMap.getOrDefault(itemKey, List.of());
    }

    private void loadItemFromFile(@NotNull File file) {
        String itemKey = file.getName().toLowerCase().replace(".yml", "");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection section = config.getConfigurationSection(itemKey);
        if (section == null) {
            plugin.getLogger().warning("Section '" + itemKey + "' missing in " + file.getName());
            return;
        }

        spawnTicksMap.put(itemKey, section.getInt("SpawnTicks", 200));

        List<Material> materials = section.getStringList("SpawningMaterials").stream()
                .map(string -> Material.matchMaterial(string.toUpperCase()))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
        spawningMaterialsMap.put(itemKey, materials);

        Material material = Material.matchMaterial(section.getString("Material", "STONE").toUpperCase());
        if (material == null || !material.isBlock()) {
            plugin.getLogger().warning("Invalid material for item " + itemKey + ": " + section.getString("Material"));
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

        String displayName = section.getString("DisplayName");
        if (displayName != null) {
            Component componentDisplayName = mini.deserialize(displayName).decoration(TextDecoration.ITALIC, false);
            itemMeta.displayName(componentDisplayName);
        }

        List<Component> lore = section.getStringList("Lore").stream()
                .map(loreLine -> mini.deserialize(loreLine).decoration(TextDecoration.ITALIC, false))
                .toList();

        if (!lore.isEmpty()) itemMeta.lore(lore);

        ConfigurationSection enchantments = section.getConfigurationSection("Enchantments");
        if (enchantments != null) {
            RegistryAccess registryAccess = RegistryAccess.registryAccess();
            for (String enchantmentKey : enchantments.getKeys(false)) {
                Enchantment enchantment = registryAccess.getRegistry(RegistryKey.ENCHANTMENT)
                        .get(NamespacedKey.minecraft(enchantmentKey.toLowerCase()));
                if (enchantment != null) itemMeta.addEnchant(enchantment, enchantments.getInt(enchantmentKey), true);
            }
        }

        boolean unbreakable = section.getBoolean("Unbreakable", true);
        ConfigurationSection flagsSection = section.getConfigurationSection("Flags");
        boolean hideEnchantments = flagsSection != null && flagsSection.getBoolean("HideEnchantments", false);
        boolean hideUnbreakable = flagsSection != null && flagsSection.getBoolean("HideUnbreakable", false);

        itemMeta.setUnbreakable(unbreakable);
        if (hideEnchantments) itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        if (hideUnbreakable) itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        NamespacedKey key = new NamespacedKey(plugin, itemKey);
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, itemKey);

        itemStack.setItemMeta(itemMeta);
        items.put(itemKey, itemStack);
        keys.put(itemKey, key);
        plugin.getLogger().info("Loaded item: " + itemKey);
    }
}
