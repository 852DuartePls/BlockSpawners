package me.duart.blockSpawners.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
@NullMarked
public class OnlinePlayersArgument implements SuggestionProvider<CommandSourceStack> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining();
        List<String> onlinePlayers = Bukkit.getOnlinePlayers()
                .stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase(Locale.ENGLISH).startsWith(input))
                .limit(5)
                .toList();

        onlinePlayers.forEach(builder::suggest);
        return builder.buildFuture();
    }
}
