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
import net.minecraft.world.Difficulty;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class DifficultyArgument implements ArgumentType<Difficulty> {
    private static final Identifier TYPE_ID = Identifier.fromNamespaceAndPath("gamemode1", "difficulty");
    private static final DynamicCommandExceptionType INVALID_DIFFICULTY =
            new DynamicCommandExceptionType(value -> Component.literal("Unknown difficulty: " + value));

    public static void register() {
        ArgumentTypeRegistry.registerArgumentType(
                TYPE_ID, DifficultyArgument.class, SingletonArgumentInfo.contextFree(DifficultyArgument::new));
    }

    @Override
    public Difficulty parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        String input = reader.readUnquotedString();
        return switch (input.toLowerCase(Locale.ROOT)) {
            case "peaceful", "p", "0" -> Difficulty.PEACEFUL;
            case "easy", "e", "1" -> Difficulty.EASY;
            case "normal", "n", "2" -> Difficulty.NORMAL;
            case "hard", "h", "3" -> Difficulty.HARD;
            default -> {
                reader.setCursor(start);
                throw INVALID_DIFFICULTY.createWithContext(reader, input);
            }
        };
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(
            CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();
        for (Difficulty difficulty : Difficulty.values()) {
            String name = difficulty.getSerializedName();
            if (name.startsWith(remaining)) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }
}
