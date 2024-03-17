package net.duart.blockspawners.commands;

import net.duart.blockspawners.managing.SpawnerBlock;
import net.duart.blockspawners.managing.SpawnerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlockSpawnersCommands implements CommandExecutor, TabCompleter {
    private final List<SpawnerBlock> loadedBlocks;
    private final SpawnerManager spawnerManager;

    public BlockSpawnersCommands(List<SpawnerBlock> loadedBlocks, SpawnerManager spawnerManager) {
        this.loadedBlocks = loadedBlocks;
        this.spawnerManager = spawnerManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Missing username parameter.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            showCommandUsage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                handleGiveCommand(player, args);
                break;
            case "reload":
                handleReloadCommand(player);
                break;
            default:
                showCommandUsage(player);
                break;
        }

        return true;
    }

    private void showCommandUsage(Player player) {
        player.sendMessage(ChatColor.RED + "Usage: /blockspawner give <bloque> [jugador]");
        player.sendMessage(ChatColor.RED + "Usage: /blockspawner reload");
    }

    private void handleGiveCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Please specify the blockspawner name.");
            return;
        }

        String blockName = args[1].toLowerCase();
        SpawnerBlock spawnerBlock = loadedBlocks.stream()
                .filter(block -> block.getFileName().equalsIgnoreCase(blockName))
                .findFirst()
                .orElse(null);

        if (spawnerBlock == null) {
            player.sendMessage("The specified block does not exist.");
            return;
        }

        ItemStack blockItem = createBlockItem(spawnerBlock);
        Player targetPlayer = (args.length >= 4) ? Bukkit.getPlayer(args[3]) : player;

        if (targetPlayer == null) {
            player.sendMessage("The specified player is not online.");
            return;
        }

        targetPlayer.getInventory().addItem(blockItem);
        player.sendMessage(ChatColor.BLUE + "Given block: " + spawnerBlock.getFormattedDisplayName() +
                ChatColor.BLUE + " to: " + targetPlayer.getName());
    }

    private ItemStack createBlockItem(SpawnerBlock spawnerBlock) {
        ItemStack blockItem = new ItemStack(spawnerBlock.getMaterial());
        ItemMeta itemMeta = blockItem.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(spawnerBlock.getFormattedDisplayName());
            itemMeta.setLore(spawnerBlock.getFormattedLore());
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            blockItem.setItemMeta(itemMeta);

            blockItem.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
        } else {
            Bukkit.getLogger().warning("Error while creating block.");
        }
        return blockItem;
    }

    private void handleReloadCommand(Player player) {
        if (!player.hasPermission("blockspawners.reload")) {
            player.sendMessage("You do not have permission to use this command.");
            return;
        }

        spawnerManager.loadSpawners();
        loadedBlocks.clear();
        loadedBlocks.addAll(spawnerManager.getLoadedBlocks());

        player.sendMessage(ChatColor.GREEN + "Spawners have been reloaded correctly.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("give");
            completions.add("reload");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            String input = args[1].toLowerCase();
            completions.addAll(loadedBlocks.stream()
                    .map(SpawnerBlock::getFileName)
                    .filter(blockName -> blockName.toLowerCase().startsWith(input))
                    .collect(Collectors.toList()));
        } else if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(playerName -> playerName.toLowerCase().startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList()));
        }

        return completions;
    }
}