package dev.gamemode1;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.GameType;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class ModeArgument implements ArgumentType<GameType> {
    private static final Identifier TYPE_ID = Identifier.fromNamespaceAndPath("gamemode1", "gamemode");
    private static final DynamicCommandExceptionType INVALID_GAMEMODE =
            new DynamicCommandExceptionType(value -> Component.literal("Unknown game mode: " + value));
    private static final List<GameType> MODES = List.of(
            GameType.SURVIVAL, GameType.CREATIVE, GameType.ADVENTURE, GameType.SPECTATOR);

    public static void register() {
        ArgumentTypeRegistry.registerArgumentType(
                TYPE_ID,
                ModeArgument.class,
                SingletonArgumentInfo.contextFree(ModeArgument::new)
        );
    }

    @Override
    public GameType parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        String input = reader.readUnquotedString();

        return switch (input.toLowerCase(Locale.ROOT)) {
            case "survival", "s", "0" -> GameType.SURVIVAL;
            case "creative", "c", "1" -> GameType.CREATIVE;
            case "adventure", "a", "2" -> GameType.ADVENTURE;
            case "spectator", "sp", "3" -> GameType.SPECTATOR;
            default -> {
                reader.setCursor(start);
                throw INVALID_GAMEMODE.createWithContext(reader, input);
            }
        };
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(
            CommandContext<S> context,
            SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();

        for (GameType mode : MODES) {
            String name = mode.getName();
            if (name.startsWith(remaining)) {
                builder.suggest(name);
            }
        }

        return builder.buildFuture();
    }
}
