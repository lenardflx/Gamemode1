package dev.gamemode1;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.Identifier;

import java.util.concurrent.CompletableFuture;

public final class LegacySlotArgument implements ArgumentType<String> {
    private static final Identifier TYPE_ID = Identifier.fromNamespaceAndPath("gamemode1", "legacy_slot");

    public static void register() {
        ArgumentTypeRegistry.registerArgumentType(
                TYPE_ID, LegacySlotArgument.class, SingletonArgumentInfo.contextFree(LegacySlotArgument::new));
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String legacy = reader.readUnquotedString();
        String modern = legacy.startsWith("slot.") ? legacy.substring("slot.".length()) : legacy;
        new SlotArgument().parse(new StringReader(modern));
        return modern;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(
            CommandContext<S> context, SuggestionsBuilder builder) {
        return new SlotArgument().listSuggestions(context, builder);
    }
}
