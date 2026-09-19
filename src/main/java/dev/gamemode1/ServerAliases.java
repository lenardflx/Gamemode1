package dev.gamemode1;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;

final class ServerAliases {
    private ServerAliases() {
    }

    static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        registerGamemode(dispatcher);
        registerDefaultGamemode(dispatcher);
        registerDifficulty(dispatcher);
        registerWeather(dispatcher);
        registerExperience(dispatcher);
        registerLocate(dispatcher, registryAccess);
        registerHelp(dispatcher);
        registerReplaceItem(dispatcher, registryAccess);
        registerGamemodeShortcuts(dispatcher);
    }

    private static void registerGamemode(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gamemode")
                .requires(Commands.hasPermission(GameModeCommand.PERMISSION_CHECK))
                .then(modeArgument(dispatcher).requires(source -> ModConfig.INSTANCE.enableGamemode)));
        dispatcher.register(Commands.literal("gm")
                .requires(source -> ModConfig.INSTANCE.enableGm
                        && Commands.hasPermission(GameModeCommand.PERMISSION_CHECK).test(source))
                .then(modeArgument(dispatcher)));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, GameType> modeArgument(
            CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.argument("legacyMode", new ModeArgument())
                .executes(context -> execute(dispatcher, context,
                        CommandText.gamemode(context.getArgument("legacyMode", GameType.class), null)))
                .then(Commands.argument("target", EntityArgument.players())
                        .executes(context -> execute(dispatcher, context,
                                CommandText.gamemode(context.getArgument("legacyMode", GameType.class),
                                        lastArgument(context)))));
    }

    private static void registerDefaultGamemode(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("defaultgamemode")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("legacyMode", new ModeArgument())
                        .requires(source -> ModConfig.INSTANCE.enableDefaultGamemode)
                        .executes(context -> execute(dispatcher, context,
                                CommandText.defaultGamemode(context.getArgument("legacyMode", GameType.class))))));
    }

    private static void registerDifficulty(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("difficulty")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("legacyDifficulty", new DifficultyArgument())
                        .requires(source -> ModConfig.INSTANCE.enableDifficulty)
                        .executes(context -> execute(dispatcher, context,
                                CommandText.difficulty(context.getArgument("legacyDifficulty", Difficulty.class))))));
    }

    private static void registerWeather(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("toggledownfall")
                .requires(source -> ModConfig.INSTANCE.enableWeather
                        && Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(source))
                .executes(context -> {
                    var weather = context.getSource().getServer().getWeatherData();
                    return execute(dispatcher, context,
                            CommandText.toggledownfall(weather.isRaining() || weather.isThundering()));
                }));
    }

    static void registerExperience(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Vanilla /xp redirects to /experience, so this child supports both roots.
        dispatcher.register(Commands.literal("experience")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("legacyAmount", new ExperienceAmountArgument())
                        .requires(source -> ModConfig.INSTANCE.enableExperience)
                        .executes(context -> execute(dispatcher, context,
                                CommandText.experience(amount(context), null)))
                        .then(Commands.argument("target", EntityArgument.players())
                                .executes(context -> execute(dispatcher, context,
                                        CommandText.experience(amount(context), lastArgument(context)))))));
    }

    private static ExperienceAmountArgument.Amount amount(CommandContext<CommandSourceStack> context) {
        return context.getArgument("legacyAmount", ExperienceAmountArgument.Amount.class);
    }

    private static void registerLocate(
            CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(Commands.literal("locate")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("legacyStructure",
                                ResourceOrTagKeyArgument.resourceOrTagKey(Registries.STRUCTURE))
                        .requires(source -> ModConfig.INSTANCE.enableLocate)
                        .executes(context -> execute(dispatcher, context,
                                CommandText.locateStructure(lastArgument(context))))));
        dispatcher.register(Commands.literal("locatebiome")
                .requires(source -> ModConfig.INSTANCE.enableLocate
                        && Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(source))
                .then(Commands.argument("biome", ResourceOrTagArgument.resourceOrTag(registryAccess, Registries.BIOME))
                        .executes(context -> execute(dispatcher, context,
                                CommandText.locateBiome(lastArgument(context))))));
    }

    private static void registerHelp(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("?")
                .requires(source -> ModConfig.INSTANCE.enableHelp)
                .executes(context -> execute(dispatcher, context, CommandText.help("")))
                .then(Commands.argument("arguments", StringArgumentType.greedyString())
                        .executes(context -> execute(dispatcher, context,
                                CommandText.help(context.getArgument("arguments", String.class))))));
    }

    private static void registerReplaceItem(
            CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(Commands.literal("replaceitem")
                .requires(source -> ModConfig.INSTANCE.enableReplaceItem
                        && Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(source))
                .then(Commands.literal("entity")
                        .then(Commands.argument("target", EntityArgument.entities())
                                .then(replaceItemTail(dispatcher, registryAccess, "entity"))))
                .then(Commands.literal("block")
                        .then(Commands.argument("target", BlockPosArgument.blockPos())
                                .then(replaceItemTail(dispatcher, registryAccess, "block")))));
    }

    private static void registerGamemodeShortcuts(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dgm")
                .requires(source -> ModConfig.INSTANCE.enableGamemodeShortcuts
                        && Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(source))
                .then(Commands.argument("legacyMode", new ModeArgument())
                        .executes(context -> execute(dispatcher, context,
                                CommandText.defaultGamemode(context.getArgument("legacyMode", GameType.class))))));
        registerGamemodeShortcut(dispatcher, "gms", GameType.SURVIVAL);
        registerGamemodeShortcut(dispatcher, "gmc", GameType.CREATIVE);
        registerGamemodeShortcut(dispatcher, "gma", GameType.ADVENTURE);
        registerGamemodeShortcut(dispatcher, "gmsp", GameType.SPECTATOR);
    }

    private static void registerGamemodeShortcut(
            CommandDispatcher<CommandSourceStack> dispatcher, String name, GameType mode) {
        dispatcher.register(Commands.literal(name)
                .requires(source -> ModConfig.INSTANCE.enableGamemodeShortcuts
                        && Commands.hasPermission(GameModeCommand.PERMISSION_CHECK).test(source))
                .executes(context -> execute(dispatcher, context, CommandText.gamemode(mode, null)))
                .then(Commands.argument("target", EntityArgument.players())
                        .executes(context -> execute(dispatcher, context,
                                CommandText.gamemode(mode, lastArgument(context))))));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> replaceItemTail(
            CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, String kind) {
        return Commands.argument("slot", new LegacySlotArgument())
                .then(Commands.argument("item", ItemArgument.item(registryAccess))
                        .executes(context -> executeReplaceItem(dispatcher, context, kind, null))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 99))
                                .executes(context -> executeReplaceItem(dispatcher, context, kind,
                                        context.getArgument("count", Integer.class)))));
    }

    private static int executeReplaceItem(CommandDispatcher<CommandSourceStack> dispatcher,
                                          CommandContext<CommandSourceStack> context, String kind, Integer count)
            throws CommandSyntaxException {
        return execute(dispatcher, context, CommandText.replaceItem(kind, argumentText(context, "target"),
                context.getArgument("slot", String.class), argumentText(context, "item"), count));
    }

    private static int execute(CommandDispatcher<CommandSourceStack> dispatcher,
                               CommandContext<CommandSourceStack> context, String command) throws CommandSyntaxException {
        return dispatcher.execute(command, context.getSource());
    }

    private static String lastArgument(CommandContext<CommandSourceStack> context) {
        return context.getNodes().getLast().getRange().get(context.getInput());
    }

    private static String argumentText(CommandContext<CommandSourceStack> context, String name) {
        return context.getNodes().stream()
                .filter(node -> node.getNode().getName().equals(name))
                .findFirst()
                .orElseThrow()
                .getRange().get(context.getInput());
    }
}
