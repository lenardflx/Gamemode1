package dev.gamemode1;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;

final class CommandText {
    private CommandText() {
    }

    static String gamemode(GameType mode, String target) {
        return "gamemode " + mode.getName() + target(target);
    }

    static String defaultGamemode(GameType mode) {
        return "defaultgamemode " + mode.getName();
    }

    static String difficulty(Difficulty difficulty) {
        return "difficulty " + difficulty.getSerializedName();
    }

    static String toggledownfall(boolean raining) {
        return raining ? "weather clear 6000" : "weather rain 6000";
    }

    static String experience(ExperienceAmountArgument.Amount amount, String target) {
        return "experience add " + (target == null ? "@s" : target) + " " + amount.value()
                + (amount.levels() ? " levels" : " points");
    }

    static String locateStructure(String structure) {
        return "locate structure " + structure;
    }

    static String locateBiome(String biome) {
        return "locate biome " + biome;
    }

    static String help(String commands) {
        return "help" + (commands.isEmpty() ? "" : " " + commands);
    }

    static String replaceItem(String kind, String target, String slot, String item, Integer count) {
        return "item replace " + kind + " " + target + " " + slot + " with " + item
                + (count == null ? "" : " " + count);
    }

    private static String target(String target) {
        return target == null ? "" : " " + target;
    }
}
