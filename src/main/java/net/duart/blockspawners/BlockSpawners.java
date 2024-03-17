package net.duart.blockspawners;

import net.duart.blockspawners.commands.BlockSpawnersCommands;
import net.duart.blockspawners.events.BlockSpawnerEvents;
import net.duart.blockspawners.managing.FolderManager;
import net.duart.blockspawners.managing.SpawnerBlock;
import net.duart.blockspawners.managing.SpawnerManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class BlockSpawners extends JavaPlugin {
    private final List<SpawnerBlock> loadedBlocks = new ArrayList<>();

    @Override
    public void onEnable() {
        getLogger().info("	__________  _________");
        getLogger().info("	\\______   \\/   _____/");
        getLogger().info("	 |    |  _/\\_____  \\");
        getLogger().info("	 |    |   \\/        \\");
        getLogger().info("	 |______  /_______  /");
        getLogger().info("	        \\/        \\/");
        getLogger().info(" BlockSpawners 1.0 by DaveDuart");
        FolderManager.createFolders(this);
        SpawnerManager spawnerManager = new SpawnerManager(this);
        spawnerManager.loadSpawners();
        loadedBlocks.addAll(spawnerManager.getLoadedBlocks());
        new BlockSpawnerEvents(this, spawnerManager);
        if (getCommand("blockspawners") != null) {
            getCommand("blockspawners").setExecutor(new BlockSpawnersCommands(loadedBlocks, spawnerManager));
        } else {
            getLogger().warning("El comando 'blockspawners' no está registrado en el plugin.yml.");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("el plugin se ha Deshabilitado!.");
    }
}
