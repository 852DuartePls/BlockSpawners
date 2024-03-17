package net.duart.blockspawners.managing;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SpawnerBlock {
    private final String fileName;
    private String displayName;
    private  Material material;
    private final List<String> lore;
    private int spawnTicks;
    private final List<Material> spawningMaterials;
    private final JavaPlugin plugin;
    private long lastModified;

    public void setMaterial(Material material) {
        this.material = material;
    }

    public void setSpawningMaterials(List<Material> spawningMaterials) {
        this.spawningMaterials.clear();
        if (spawningMaterials != null) {
            this.spawningMaterials.addAll(spawningMaterials);
        }
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public SpawnerBlock(String fileName, String displayName, Material material, List<String> lore, Integer spawnTicks, List<Material> spawningMaterials, JavaPlugin plugin) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }
        if (material == null) {
            throw new IllegalArgumentException("Spawner material cannot be empty.");
        }

        this.fileName = fileName;
        this.displayName = (displayName == null || displayName.isEmpty()) ? "Unnamed Block" : displayName;
        this.material = material;
        this.lore = (lore == null) ? new ArrayList<>() : new ArrayList<>(lore); // Copiar la lista para evitar modificaciones externas
        this.plugin = plugin;

        // Verificar si spawnTicks es nulo o negativo
        if (spawnTicks == null || spawnTicks <= 0) {
            this.spawnTicks = 200; // Valor por defecto 200 si spawnTicks es nulo o 0
        } else {
            this.spawnTicks = spawnTicks;
        }

        // Verificar si spawningMaterials es nulo
        if (spawningMaterials == null) {
            this.spawningMaterials = new ArrayList<>();
            this.spawningMaterials.add(Material.COBBLESTONE); // Agregar cobblestone como material por defecto si spawningMaterials es null
        } else {
            this.spawningMaterials = new ArrayList<>(spawningMaterials); // Copiar la lista para evitar modificaciones externas
        }

    }

    public long getLastModified(String blocksFolderPath) {
        String filePath = blocksFolderPath + File.separator + fileName + ".yml";
        File file = new File(filePath);
        long lastModified = file.lastModified(); //
        return lastModified;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = (displayName == null || displayName.isEmpty()) ? "Unnamed Block" : displayName;
    }

    public Material getMaterial() {
        return material;
    }


    public List<String> getLore() {
        return new ArrayList<>(lore);
    }

    public void setLore(List<String> lore) {
        this.lore.clear();
        if (lore != null) {
            this.lore.addAll(lore);
        }
    }

    public int getSpawnTicks() {
        return spawnTicks;
    }

    public void setSpawnTicks(int spawnTicks) {
        this.spawnTicks = spawnTicks;
    }

    public List<Material> getSpawningMaterials() {
        return new ArrayList<>(spawningMaterials);
    }

    public String getFormattedDisplayName() {
        return ChatColor.translateAlternateColorCodes('&', displayName);
    }

    public List<String> getFormattedLore() {
        List<String> formattedLore = new ArrayList<>();
        for (String line : lore) {
            formattedLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        return formattedLore;
    }

    public void updateBlock(String displayName, List<String> lore, Integer spawnTicks, List<Material> spawningMaterials) {
        boolean modified = false;

        if (displayName != null && !displayName.isEmpty() && !displayName.equals(this.displayName)) {
            this.displayName = displayName;
            modified = true;
        }

        if (lore != null && !lore.equals(this.lore)) {
            this.lore.clear();
            this.lore.addAll(lore);
            modified = true;
        }

        if (spawnTicks != null && spawnTicks > 0 && spawnTicks != this.spawnTicks) {
            this.spawnTicks = spawnTicks;
            modified = true;
        }

        if (spawningMaterials != null && !spawningMaterials.equals(this.spawningMaterials)) {
            this.spawningMaterials.clear();
            this.spawningMaterials.addAll(spawningMaterials);
            modified = true;
        }

        if (modified) {
            plugin.getLogger().info("Block information has been updated " + fileName);
        } else {
            plugin.getLogger().info("No changes found in the block: " + fileName + " to update.");
        }
    }

    @Override
    public String toString() {
        return "SpawnerBlock{" +
                "fileName='" + fileName + '\'' +
                ", displayName='" + displayName + '\'' +
                ", material=" + material +
                ", lore=" + lore +
                ", spawnTicks=" + spawnTicks +
                ", spawningMaterials=" + spawningMaterials +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpawnerBlock that = (SpawnerBlock) o;
        return fileName.equals(that.fileName) &&
                displayName.equals(that.displayName) &&
                material == that.material &&
                lore.equals(that.lore) &&
                spawnTicks == that.spawnTicks &&
                spawningMaterials.equals(that.spawningMaterials);
    }

    @Override
    public int hashCode() {
        int result = fileName.hashCode();
        result = 31 * result + displayName.hashCode();
        result = 31 * result + material.hashCode();
        result = 31 * result + lore.hashCode();
        result = 31 * result + spawnTicks;
        result = 31 * result + spawningMaterials.hashCode();
        return result;
    }


}
