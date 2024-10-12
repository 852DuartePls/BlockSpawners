package me.duart.blockSpawners.commands;

import me.duart.blockSpawners.BlockSpawners;
import me.duart.blockSpawners.manager.LoadBlockSpawners;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@NullMarked
public class BlockSpawnerCommands implements CommandExecutor, TabCompleter {
    private final BlockSpawners plugin;
    private final LoadBlockSpawners loadBlockSpawners;
    private final MiniMessage mini = MiniMessage.miniMessage();
    private final String permission = "blockspawners.admin";

    public BlockSpawnerCommands(LoadBlockSpawners loadBlockSpawners, BlockSpawners plugin) {
        this.plugin = plugin;
        this.loadBlockSpawners = loadBlockSpawners;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String pluginVersion = plugin.getPluginMeta().getVersion();
        var plVerComponent = mini.deserialize(" <green>v" + pluginVersion + "</green>");
        var announcerPrefix = mini.deserialize("<white>[<color:#9a63ff>BlockSpawners</color>]</white>");
        var noPlayerFound = mini.deserialize("<red> Player not found!</red>");

        if (args.length == 0) {
            sender.sendMessage(announcerPrefix.append(plVerComponent));
            return true;
        }

        String commandName = args[0].toLowerCase();
        return switch (commandName) {
            case "give" -> handleGive(sender, announcerPrefix, noPlayerFound, args);
            case "reload" -> handleReload(sender, announcerPrefix);
            default -> false;
        };
    }

    private boolean handleGive(CommandSender sender, Component announcerPrefix, Component noPlayerFound, String[] args) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(announcerPrefix.append(mini.deserialize("<red> You don't have permission to use this command.</red>")));
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage(announcerPrefix.append(mini.deserialize("<red> You must specify an item key.</red>")));
            return false;
        }

        String itemKey = args[1];
        Player targetPlayer = getTargetPlayer(sender, args);

        if (targetPlayer == null) {
            sender.sendMessage(noPlayerFound);
            return false;
        }

        ItemStack item = loadBlockSpawners.getItem(itemKey);
        if (item == null) {
            sender.sendMessage(announcerPrefix.append(mini.deserialize("<red> Item not found: " + itemKey + "</red>")));
            return true;
        }

        targetPlayer.getInventory().addItem(item);
        targetPlayer.sendMessage(announcerPrefix.append(mini.deserialize("<green> You have been given:<color:#864aff> " + itemKey + "</color>")));
        sender.sendMessage(announcerPrefix.append(mini.deserialize("<green> Given<color:#864aff> " + itemKey + "</color> to " + targetPlayer.getName() + "</green>")));
        return true;
    }

    private boolean handleReload(CommandSender sender, Component announcerPrefix) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(announcerPrefix.append(mini.deserialize("<red>You don't have permission to use this command.</red>")));
            return false;
        }

        sender.sendMessage(announcerPrefix.append(mini.deserialize("<green> Reloading...</green>")));

        plugin.onReload().thenAccept(result -> {
            sender.sendMessage(announcerPrefix.append(mini.deserialize("<green> Reload completed successfully!</green>")));
        }).exceptionally(ex -> {
            plugin.getLogger().severe("An error occurred during reload: " + ex.getMessage());
            sender.sendMessage(announcerPrefix.append(mini.deserialize("<red> Reload failed. Check console for details.</red>")));
            return null;
        });

        return true;
    }

    private @Nullable Player getTargetPlayer(CommandSender sender, String [] args) {
        if (args.length == 3) {
            return Bukkit.getPlayer(args[2]);
        } else if (sender instanceof Player) {
            return (Player) sender;
        } else {
            sender.sendMessage(mini.deserialize("<red>You must specify a player when running this command from the console!"));
            return null;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission(permission)) return completions;
        if (args.length == 1) {
            completions.add("give");
            completions.add("reload");
        } else if (args.length == 2) {
            if ("give".equalsIgnoreCase(args[0])) {
                completions.addAll(loadBlockSpawners.getItemKeys());
            }
        } else if (args.length == 3) {
            if ("give".equalsIgnoreCase(args[0])) {
                completions.addAll(Bukkit.getOnlinePlayers()
                        .stream()
                        .map(Player::getName)
                        .toList());
            }
        }

        return completions;
    }
}
