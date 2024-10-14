package me.duart.blockSpawners.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import me.duart.blockSpawners.manager.LoadBlockSpawners;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
@NullMarked
public class ItemKeyArgument implements CustomArgumentType.Converted<String, String> {
    private final LoadBlockSpawners loadSpawners;
    private final MiniMessage mini = MiniMessage.miniMessage();
    private final Component ItemNotFound = mini.deserialize("<red>Item not found: ");

    public ItemKeyArgument(LoadBlockSpawners loadSpawners) {
        this.loadSpawners = loadSpawners;
    }

    @Override
    public String convert(String nativeType) throws CommandSyntaxException {
        List<String> itemKeys = loadSpawners.getItemKeys();
        if (itemKeys.contains(nativeType)) {
            return nativeType;
        } else {
            Message message = MessageComponentSerializer.message().serialize(Component.text(ItemNotFound + nativeType));
            throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
        }
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining();
        loadSpawners.getItemKeys()
                .stream()
                .filter(key -> key.startsWith(input))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
