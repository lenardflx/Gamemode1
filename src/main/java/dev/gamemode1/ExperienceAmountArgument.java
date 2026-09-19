package dev.gamemode1;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class ExperienceAmountArgument implements ArgumentType<ExperienceAmountArgument.Amount> {
    private static final Identifier TYPE_ID = Identifier.fromNamespaceAndPath("gamemode1", "experience_amount");
    private static final DynamicCommandExceptionType INVALID_AMOUNT =
            new DynamicCommandExceptionType(value -> Component.literal("Invalid experience amount: " + value));

    public record Amount(int value, boolean levels) {
    }

    public static void register() {
        ArgumentTypeRegistry.registerArgumentType(TYPE_ID, ExperienceAmountArgument.class,
                SingletonArgumentInfo.contextFree(ExperienceAmountArgument::new));
    }

    @Override
    public Amount parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        String input = reader.readUnquotedString();
        boolean levels = input.endsWith("L");
        String number = levels ? input.substring(0, input.length() - 1) : input;
        try {
            return new Amount(Integer.parseInt(number), levels);
        } catch (NumberFormatException e) {
            reader.setCursor(start);
            throw INVALID_AMOUNT.createWithContext(reader, input);
        }
    }
}
