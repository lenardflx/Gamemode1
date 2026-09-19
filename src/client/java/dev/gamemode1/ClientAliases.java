package dev.gamemode1;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;

final class ClientAliases {
    private ClientAliases() {
    }

    static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        registerGamemode(dispatcher, "gamemode");
        registerGamemode(dispatcher, "gm");
        registerDefaultGamemode(dispatcher);
        registerDifficulty(dispatcher);
        registerWeather(dispatcher);
        registerExperience(dispatcher, "xp");
        registerExperience(dispatcher, "experience");
        registerLocate(dispatcher, registryAccess);
        registerHelp(dispatcher);
        registerReplaceItem(dispatcher, registryAccess);
        registerGamemodeShortcuts(dispatcher);
    }

    private static void registerGamemode(CommandDispatcher<FabricClientCommandSource> dispatcher, String name) {
        dispatcher.register(ClientCommands.literal(name)
                .requires(source -> name.equals("gm")
                        ? enabledFor(source, ModConfig.INSTANCE.enableGm, "gamemode")
                        : enabledFor(source, ModConfig.INSTANCE.enableGamemode, "gamemode"))
                .then(ClientCommands.argument("legacyMode", new ModeArgument())
                        .executes(context -> forward(context,
                                CommandText.gamemode(context.getArgument("legacyMode", GameType.class), null)))
                        .then(ClientCommands.argument("target", EntityArgument.players())
                                .executes(context -> forward(context,
                                        CommandText.gamemode(context.getArgument("legacyMode", GameType.class),
                                                lastArgument(context)))))));
    }

    private static void registerDefaultGamemode(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("defaultgamemode")
                .requires(source -> enabledFor(source, ModConfig.INSTANCE.enableDefaultGamemode, "defaultgamemode"))
                .then(ClientCommands.argument("legacyMode", new ModeArgument())
                        .executes(context -> forward(context,
                                CommandText.defaultGamemode(context.getArgument("legacyMode", GameType.class))))));
    }

    private static void registerDifficulty(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("difficulty")
                .requires(source -> enabledFor(source, ModConfig.INSTANCE.enableDifficulty, "difficulty"))
                .then(ClientCommands.argument("legacyDifficulty", new DifficultyArgument())
                        .executes(context -> forward(context,
                                CommandText.difficulty(context.getArgument("legacyDifficulty", Difficulty.class))))));
    }

    private static void registerWeather(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("toggledownfall")
                .requires(source -> enabledFor(source, ModConfig.INSTANCE.enableWeather, "weather"))
                .executes(context -> forward(context,
                        CommandText.toggledownfall(context.getSource().getLevel().isRaining()
                                || context.getSource().getLevel().isThundering()))));
    }

    private static void registerExperience(CommandDispatcher<FabricClientCommandSource> dispatcher, String name) {
        dispatcher.register(ClientCommands.literal(name)
                .requires(source -> enabledFor(source, ModConfig.INSTANCE.enableExperience, "experience"))
                .then(experienceAmount()
                        .executes(context -> forward(context, CommandText.experience(amount(context), null)))
                        .then(ClientCommands.argument("target", EntityArgument.players())
                                .executes(context -> forward(context,
                                        CommandText.experience(amount(context), lastArgument(context)))))));
    }

    private static RequiredArgumentBuilder<FabricClientCommandSource, ExperienceAmountArgument.Amount>
    experienceAmount() {
        return ClientCommands.argument("legacyAmount", new ExperienceAmountArgument());
    }

    private static ExperienceAmountArgument.Amount amount(CommandContext<FabricClientCommandSource> context) {
        return context.getArgument("legacyAmount", ExperienceAmountArgument.Amount.class);
    }

    private static void registerLocate(
            CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(ClientCommands.literal("locate")
                .requires(source -> enabledFor(source, ModConfig.INSTANCE.enableLocate, "locate"))
                .then(ClientCommands.argument("legacyStructure",
                                ResourceOrTagKeyArgument.resourceOrTagKey(Registries.STRUCTURE))
                        .executes(context -> forward(context,
                                CommandText.locateStructure(lastArgument(context))))));
        dispatcher.register(ClientCommands.literal("locatebiome")
                .requires(source -> enabledFor(source, ModConfig.INSTANCE.enableLocate, "locate"))
                .then(ClientCommands.argument("biome",
                                ResourceOrTagArgument.resourceOrTag(registryAccess, Registries.BIOME))
                        .executes(context -> forward(context,
                                CommandText.locateBiome(lastArgument(context))))));
    }

    private static void registerHelp(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("?")
                .requires(source -> enabledFor(source, ModConfig.INSTANCE.enableHelp, "help"))
                .executes(context -> forward(context, CommandText.help("")))
                .then(ClientCommands.argument("arguments", StringArgumentType.greedyString())
                        .executes(context -> forward(context,
                                CommandText.help(context.getArgument("arguments", String.class))))));
    }

    private static void registerReplaceItem(
            CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(ClientCommands.literal("replaceitem")
                .requires(source -> enabledFor(source, ModConfig.INSTANCE.enableReplaceItem, "item"))
                .then(ClientCommands.literal("entity")
                        .then(ClientCommands.argument("target", EntityArgument.entities())
                                .then(replaceItemTail(registryAccess, "entity"))))
                .then(ClientCommands.literal("block")
                        .then(ClientCommands.argument("target", BlockPosArgument.blockPos())
                                .then(replaceItemTail(registryAccess, "block")))));
    }

    private static void registerGamemodeShortcuts(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("dgm")
                .requires(source -> enabledFor(source, ModConfig.INSTANCE.enableGamemodeShortcuts,
                        "defaultgamemode"))
                .then(ClientCommands.argument("legacyMode", new ModeArgument())
                        .executes(context -> forward(context,
                                CommandText.defaultGamemode(context.getArgument("legacyMode", GameType.class))))));
        registerGamemodeShortcut(dispatcher, "gms", GameType.SURVIVAL);
        registerGamemodeShortcut(dispatcher, "gmc", GameType.CREATIVE);
        registerGamemodeShortcut(dispatcher, "gma", GameType.ADVENTURE);
        registerGamemodeShortcut(dispatcher, "gmsp", GameType.SPECTATOR);
    }

    private static void registerGamemodeShortcut(
            CommandDispatcher<FabricClientCommandSource> dispatcher, String name, GameType mode) {
        dispatcher.register(ClientCommands.literal(name)
                .requires(source -> enabledFor(source, ModConfig.INSTANCE.enableGamemodeShortcuts, "gamemode"))
                .executes(context -> forward(context, CommandText.gamemode(mode, null)))
                .then(ClientCommands.argument("target", EntityArgument.players())
                        .executes(context -> forward(context,
                                CommandText.gamemode(mode, lastArgument(context))))));
    }

    private static RequiredArgumentBuilder<FabricClientCommandSource, String> replaceItemTail(
            CommandBuildContext registryAccess, String kind) {
        return ClientCommands.argument("slot", new LegacySlotArgument())
                .then(ClientCommands.argument("item", ItemArgument.item(registryAccess))
                        .executes(context -> executeReplaceItem(context, kind, null))
                        .then(ClientCommands.argument("count", IntegerArgumentType.integer(1, 99))
                                .executes(context -> executeReplaceItem(context, kind,
                                        context.getArgument("count", Integer.class)))));
    }

    private static int executeReplaceItem(CommandContext<FabricClientCommandSource> context, String kind, Integer count) {
        return forward(context, CommandText.replaceItem(kind, argumentText(context, "target"),
                context.getArgument("slot", String.class), argumentText(context, "item"), count));
    }

    private static int forward(CommandContext<FabricClientCommandSource> context, String canonical) {
        ClientPacketListener connection = context.getSource().getPlayer().connection;
        connection.getConnection().send(new ServerboundChatCommandPacket(canonical));
        return 1;
    }

    private static boolean enabledFor(FabricClientCommandSource source, boolean enabled, String serverCommand) {
        return enabled && source.getPlayer().connection.getCommands().getRoot().getChild(serverCommand) != null;
    }

    private static String lastArgument(CommandContext<FabricClientCommandSource> context) {
        return context.getNodes().getLast().getRange().get(context.getInput());
    }

    private static String argumentText(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getNodes().stream()
                .filter(node -> node.getNode().getName().equals(name))
                .findFirst()
                .orElseThrow()
                .getRange().get(context.getInput());
    }
}
