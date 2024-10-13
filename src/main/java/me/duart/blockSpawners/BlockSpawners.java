package me.duart.blockSpawners;

import me.duart.blockSpawners.commands.BlockSpawnerCommands;
import me.duart.blockSpawners.events.BlockSpawnerEvents;
import me.duart.blockSpawners.manager.LoadBlockSpawners;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public final class BlockSpawners extends JavaPlugin {
    private final MiniMessage mini = MiniMessage.miniMessage();
    private final ConsoleCommandSender console = getServer().getConsoleSender();
    private final Component PluginPrefix = mini.deserialize("<white>[<color:#9a63ff>BlockSpawners</color>]</white>");
    private final Component PluginEnabled = PluginPrefix.append(mini.deserialize("<green> Enabled!"));
    private LoadBlockSpawners loadBlockSpawners;
    private BlockSpawnerEvents blockSpawnerEvents;

    @Override
    public void onEnable() {
        console.sendMessage(PluginEnabled);
        loadBlockSpawners = new LoadBlockSpawners(this);
        blockSpawnerEvents = new BlockSpawnerEvents(this, loadBlockSpawners);
        BlockSpawnerCommands blockSpawnerCommands = new BlockSpawnerCommands(loadBlockSpawners, this);
        blockSpawnerCommands.registerCommands(this);
        getServer().getPluginManager().registerEvents(blockSpawnerEvents, this);
        blockSpawnerEvents.loadSpawnersFromFile();
    }

    public CompletableFuture<Void> onReload() {
       return blockSpawnerEvents.stopAllTasks().thenRun(() ->
                loadBlockSpawners.reloadSpawnerFiles().thenRun(() ->
                        Bukkit.getScheduler().runTask(this, () ->
                                this.getLogger().info("Finished reloading spawner files."))));
    }

}
