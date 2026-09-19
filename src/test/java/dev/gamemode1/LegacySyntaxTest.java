package dev.gamemode1;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.commands.ExperienceCommand;
import net.minecraft.server.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class LegacySyntaxTest {
    @Test
    public void gameModesAcceptNamesShortFormsAndNumbers() throws CommandSyntaxException {
        ModeArgument argument = new ModeArgument();
        String[] names = {"survival", "creative", "adventure", "spectator"};
        String[] shortForms = {"s", "c", "a", "sp"};
        GameType[] modes = {GameType.SURVIVAL, GameType.CREATIVE, GameType.ADVENTURE, GameType.SPECTATOR};
        for (int i = 0; i < modes.length; i++) {
            assertEquals(modes[i], argument.parse(new StringReader(names[i])));
            assertEquals(modes[i], argument.parse(new StringReader(shortForms[i])));
            assertEquals(modes[i], argument.parse(new StringReader(Integer.toString(i))));
        }
        assertEquals(GameType.CREATIVE, argument.parse(new StringReader("C")));
        assertThrows(CommandSyntaxException.class, () -> argument.parse(new StringReader("4")));
        assertEquals(List.of("creative"), argument.listSuggestions(null, new SuggestionsBuilder("c", 0))
                .join().getList().stream().map(suggestion -> suggestion.getText()).toList());
    }

    @Test
    public void difficultyAcceptsNamesShortFormsAndNumbers() throws CommandSyntaxException {
        DifficultyArgument argument = new DifficultyArgument();
        String[] names = {"peaceful", "easy", "normal", "hard"};
        String[] shortForms = {"p", "e", "n", "h"};
        Difficulty[] values = Difficulty.values();
        for (int i = 0; i < values.length; i++) {
            assertEquals(values[i], argument.parse(new StringReader(names[i])));
            assertEquals(values[i], argument.parse(new StringReader(shortForms[i])));
            assertEquals(values[i], argument.parse(new StringReader(Integer.toString(i))));
        }
        assertThrows(CommandSyntaxException.class, () -> argument.parse(new StringReader("4")));
        assertEquals(List.of("peaceful"), argument.listSuggestions(null, new SuggestionsBuilder("p", 0))
                .join().getList().stream().map(suggestion -> suggestion.getText()).toList());
    }

    @Test
    public void experienceDistinguishesPointsAndLevels() throws CommandSyntaxException {
        ExperienceAmountArgument argument = new ExperienceAmountArgument();
        var points = argument.parse(new StringReader("-12"));
        var levels = argument.parse(new StringReader("+3L"));
        assertEquals(-12, points.value());
        assertFalse(points.levels());
        assertEquals(3, levels.value());
        assertTrue(levels.levels());
        assertEquals("experience add @s -12 points", CommandText.experience(points, null));
        assertEquals("experience add @a 3 levels", CommandText.experience(levels, "@a"));
        assertThrows(CommandSyntaxException.class, () -> argument.parse(new StringReader("3l")));
        assertThrows(CommandSyntaxException.class, () -> argument.parse(new StringReader("2147483648L")));
    }

    @Test
    public void toggledownfallUsesLegacyWeatherDuration() {
        assertEquals("weather clear 6000", CommandText.toggledownfall(true));
        assertEquals("weather rain 6000", CommandText.toggledownfall(false));
    }

    @Test
    public void legacySlotsAndCommandTextUseCurrentItemSyntax() throws CommandSyntaxException {
        LegacySlotArgument argument = new LegacySlotArgument();
        assertEquals("hotbar.0", argument.parse(new StringReader("slot.hotbar.0")));
        assertEquals("item replace entity @s hotbar.0 with minecraft:stone 3",
                CommandText.replaceItem("entity", "@s", "hotbar.0", "minecraft:stone", 3));
        assertEquals("help 2", CommandText.help("2"));
    }

    @Test
    public void experienceShorthandPreservesVanillaSubcommandsAndXpRedirect() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        ExperienceCommand.register(dispatcher);
        ServerAliases.registerExperience(dispatcher);

        var experience = dispatcher.getRoot().getChild("experience");
        assertSame(experience, dispatcher.getRoot().getChild("xp").getRedirect());
        for (String name : List.of("add", "set", "query", "legacyAmount")) {
            assertNotNull(experience.getChild(name));
        }
    }

    @Test
    public void legacyBranchesCoexistWithVanillaCommandTree() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var context = Commands.createValidationContext(VanillaRegistries.createWorldLookup());
        var dispatcher = new Commands(Commands.CommandSelection.ALL, context).getDispatcher();
        ServerAliases.register(dispatcher, context);

        var root = dispatcher.getRoot();
        assertNotNull(root.getChild("gamemode").getChild("legacyMode"));
        assertNotNull(root.getChild("gm").getChild("legacyMode"));
        assertNotNull(root.getChild("defaultgamemode").getChild("legacyMode"));
        assertNotNull(root.getChild("difficulty").getChild("legacyDifficulty"));
        assertNotNull(root.getChild("toggledownfall"));
        assertNotNull(root.getChild("locate").getChild("legacyStructure"));
        assertNotNull(root.getChild("locate").getChild("structure"));
        assertNotNull(root.getChild("locate").getChild("biome"));
        assertNotNull(root.getChild("locate").getChild("poi"));
        assertNotNull(root.getChild("locatebiome").getChild("biome"));
        assertNotNull(root.getChild("?"));
        assertNotNull(root.getChild("replaceitem").getChild("entity"));
        assertNotNull(root.getChild("replaceitem").getChild("block"));
        assertNotNull(root.getChild("dgm").getChild("legacyMode"));
        for (String name : List.of("gms", "gmc", "gma", "gmsp")) {
            assertNotNull(root.getChild(name));
        }
    }
}
