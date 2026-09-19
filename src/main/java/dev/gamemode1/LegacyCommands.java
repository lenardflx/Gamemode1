package dev.gamemode1;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class LegacyCommands implements ModInitializer {
    @Override
    public void onInitialize() {
        ModeArgument.register();
        DifficultyArgument.register();
        ExperienceAmountArgument.register();
        LegacySlotArgument.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ServerAliases.register(dispatcher, registryAccess));
    }
}
