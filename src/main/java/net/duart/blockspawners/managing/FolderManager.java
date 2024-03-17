package net.duart.blockspawners.managing;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FolderManager {

    public static void createFolders(JavaPlugin plugin) {
        File pluginFolder = plugin.getDataFolder().getParentFile();
        File blockSpawnersFolder = new File(pluginFolder, "BlockSpawners");
        File blocksFolder = new File(blockSpawnersFolder, "blocks");
        createFolderIfNotExists(blockSpawnersFolder);
        createFolderIfNotExists(blocksFolder);
        copyResourceIfNotExists(plugin, "spawnerclay.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnerconcrete.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnercoral.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnerdirt.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnerglass.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnerstainedglass.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnerglazed.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnerglowstone.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnergranite.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnergrass.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnerwood.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnermycelium.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnernetherrack.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnerobsidian.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnerprismarine.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnerquartz.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnersand.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnersealantern.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnerterracotta.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "spawnerwool.yml", blocksFolder);
        copyResourceIfNotExists(plugin, "info.yml", blockSpawnersFolder);


    }

    private static void createFolderIfNotExists(File folder) {
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new RuntimeException("Could not create folder: " + folder.getAbsolutePath());
            }
        }
    }

    private static void copyResourceIfNotExists(JavaPlugin plugin, String resourceName, File destinationFolder) {
        File resourceFile = new File(destinationFolder, resourceName);

        if (!resourceFile.exists()) {
            try {
                createFolderIfNotExists(destinationFolder);
                Path resourcePath = resourceFile.toPath();
                InputStream inputStream = plugin.getResource(resourceName);
                if (inputStream != null) {
                    Files.copy(inputStream, resourcePath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    plugin.getLogger().warning("The plugin cannot find the resource:" + resourceName);
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Error while copying file " + resourceName + ": " + e.getMessage());
            }
        }
    }
}