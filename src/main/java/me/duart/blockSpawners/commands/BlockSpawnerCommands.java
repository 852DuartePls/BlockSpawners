package me.duart.blockSpawners.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.duart.blockSpawners.BlockSpawners;
import me.duart.blockSpawners.manager.LoadBlockSpawners;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class BlockSpawnerCommands {
    private final MiniMessage mini = MiniMessage.miniMessage();
    private final BlockSpawners plugin;
    private final LoadBlockSpawners loadBlockSpawners;
    private final String permission = "blockspawners.admin";
    private final String pluginVersion = BlockSpawners.getPlugin(BlockSpawners.class).getPluginMeta().getVersion();
    private final String pluginVersionFormat = "<green> v" + pluginVersion + "</green>";
    private final String announcerPrefix = "<white>[<color:#9a63ff>BlockSpawners</color>]</white>";

    public BlockSpawnerCommands(LoadBlockSpawners loadBlockSpawners, BlockSpawners plugin) {
        this.plugin = plugin;
        this.loadBlockSpawners = loadBlockSpawners;
    }

    public void registerCommands(final @NotNull JavaPlugin plugin) {
        final LifecycleEventManager<@NotNull Plugin> lifecycleManager = plugin.getLifecycleManager();
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();

            commands.register(plugin.getPluginMeta(), Commands.literal("blockspawners")
                            .executes(context -> {
                                context.getSource().getSender().sendRichMessage(announcerPrefix + pluginVersionFormat);
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(Commands.literal("give")
                                    .requires(source -> source.getSender().hasPermission(permission))
                                    .then(Commands.argument("itemKey", new ItemKeyArgument(loadBlockSpawners))
                                            .then(Commands.argument("targetName", StringArgumentType.word())
                                                    .suggests(new OnlinePlayersArgument())
                                                    .executes(context -> {
                                                        String itemKey = StringArgumentType.getString(context, "itemKey");
                                                        String targetName = StringArgumentType.getString(context, "targetName");
                                                        CommandSender sender = context.getSource().getSender();
                                                        handleGive(sender, itemKey, targetName);
                                                        return Command.SINGLE_SUCCESS;
                                                    })
                                            )
                                            .executes(context -> {  // Handle case where targetName is not provided
                                                String itemKey = StringArgumentType.getString(context, "itemKey");
                                                CommandSender sender = context.getSource().getSender();
                                                String targetName;

                                                if (sender instanceof Player) {
                                                    targetName = sender.getName();
                                                } else {
                                                    sender.sendMessage(mini.deserialize("<red>You must specify a target player when using this command from the console.</red>"));
                                                    return 0;
                                                }

                                                handleGive(sender, itemKey, targetName);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                            .then(Commands.literal("reload")
                                    .requires(source -> source.getSender().hasPermission(permission))
                                    .executes(context -> {
                                        CommandSender sender = context.getSource().getSender();
                                        handleReload(sender);
                                        return Command.SINGLE_SUCCESS;
                                    }))
                            .build(),
                    null,
                    List.of("bs")
            );
        });
    }

    private void handleGive(CommandSender sender, String itemKey, String targetName) {
        var announcerPrefixComponent = mini.deserialize(announcerPrefix);
        Player targetPlayer = targetName != null ? Bukkit.getPlayer(targetName) : null;

        if (targetPlayer == null && targetName != null) {
            String noPlayerFound = announcerPrefix + "<red> Player not found!</red>";
            sender.sendMessage(mini.deserialize(noPlayerFound));
            return;
        }

        ItemStack item = loadBlockSpawners.getItem(itemKey);
        if (item == null) {
            sender.sendMessage(announcerPrefixComponent.append(mini.deserialize("<red> Item not found: " + itemKey + "</red>")));
            return;
        }

        if (targetPlayer == null) {
            sender.sendMessage(mini.deserialize("<red>You must specify a target player when using this command from the console.</red>"));
            return;
        }

        targetPlayer.getInventory().addItem(item);
        targetPlayer.sendMessage(announcerPrefixComponent.append(mini.deserialize("<green> You have been given:<color:#864aff> " + itemKey + "</color>")));
        sender.sendMessage(announcerPrefixComponent.append(mini.deserialize("<green> Given<color:#864aff> " + itemKey + "</color> to " + targetPlayer.getName() + "</green>")));
    }

    private void handleReload(@NotNull CommandSender sender) {
        var announcerPrefixComponent = mini.deserialize(announcerPrefix);
        sender.sendMessage(announcerPrefixComponent.append(mini.deserialize("<green> Reloading...</green>")));

        plugin.onReload().thenAccept(result -> sender.sendMessage(announcerPrefixComponent.append(mini.deserialize("<green> Reload completed successfully!</green>")))).exceptionally(ex -> {
            plugin.getLogger().severe("An error occurred during reload: " + ex.getMessage());
            sender.sendMessage(announcerPrefixComponent.append(mini.deserialize("<red> Reload failed. Check console for details.</red>")));
            return null;
        });
    }
}
