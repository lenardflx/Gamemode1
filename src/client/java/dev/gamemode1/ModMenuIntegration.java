package dev.gamemode1;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreen::new;
    }

    private static final class ConfigScreen extends Screen {
        private final Screen parent;
        private boolean enableGamemode = ModConfig.INSTANCE.enableGamemode;
        private boolean enableGm = ModConfig.INSTANCE.enableGm;
        private boolean enableDefaultGamemode = ModConfig.INSTANCE.enableDefaultGamemode;
        private boolean enableDifficulty = ModConfig.INSTANCE.enableDifficulty;
        private boolean enableWeather = ModConfig.INSTANCE.enableWeather;
        private boolean enableExperience = ModConfig.INSTANCE.enableExperience;
        private boolean enableLocate = ModConfig.INSTANCE.enableLocate;
        private boolean enableHelp = ModConfig.INSTANCE.enableHelp;
        private boolean enableReplaceItem = ModConfig.INSTANCE.enableReplaceItem;
        private boolean enableGamemodeShortcuts = ModConfig.INSTANCE.enableGamemodeShortcuts;

        private ConfigScreen(Screen parent) {
            super(Component.literal("Legacy Commands Settings"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            int buttonWidth = 170;
            int gap = 10;
            int x = width / 2 - buttonWidth - gap / 2;
            int y = height / 2 - 68;

            addToggle("Legacy gamemode values", enableGamemode, x, y,
                    value -> enableGamemode = value);
            addToggle("Game mode shortcuts", enableGamemodeShortcuts, x + buttonWidth + gap, y,
                    value -> enableGamemodeShortcuts = value);

            addToggle("/gm alias", enableGm, x, y + 24,
                    value -> enableGm = value);
            addToggle("Legacy default gamemode", enableDefaultGamemode, x + buttonWidth + gap, y + 24,
                    value -> enableDefaultGamemode = value);

            addToggle("Legacy difficulty", enableDifficulty, x, y + 48,
                    value -> enableDifficulty = value);
            addToggle("/toggledownfall", enableWeather, x + buttonWidth + gap, y + 48,
                    value -> enableWeather = value);

            addToggle("Legacy /xp syntax", enableExperience, x, y + 72,
                    value -> enableExperience = value);
            addToggle("Legacy locate syntax", enableLocate, x + buttonWidth + gap, y + 72,
                    value -> enableLocate = value);

            addToggle("/? alias", enableHelp, x, y + 96,
                    value -> enableHelp = value);
            addToggle("Legacy /replaceitem", enableReplaceItem, x + buttonWidth + gap, y + 96,
                    value -> enableReplaceItem = value);

            addRenderableWidget(Button.builder(Component.literal("Done"), button -> saveAndClose())
                    .bounds(width / 2 - 100, y + 132, 200, 20)
                    .build());
        }

        private void addToggle(String label, boolean initial, int x, int y, Consumer<Boolean> change) {
            addRenderableWidget(CycleButton.onOffBuilder(initial)
                    .create(x, y, 170, 20, Component.literal(label),
                            (button, value) -> change.accept(value)));
        }

        @Override
        public void extractRenderState(
                net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            graphics.centeredText(font, title, width / 2, height / 2 - 92, 0xFFFFFF);
            super.extractRenderState(graphics, mouseX, mouseY, delta);
        }

        private void saveAndClose() {
            ModConfig config = ModConfig.INSTANCE;
            config.enableGamemode = enableGamemode;
            config.enableGm = enableGm;
            config.enableDefaultGamemode = enableDefaultGamemode;
            config.enableDifficulty = enableDifficulty;
            config.enableWeather = enableWeather;
            config.enableExperience = enableExperience;
            config.enableLocate = enableLocate;
            config.enableHelp = enableHelp;
            config.enableReplaceItem = enableReplaceItem;
            config.enableGamemodeShortcuts = enableGamemodeShortcuts;
            config.save();

            IntegratedServer server = minecraft.getSingleplayerServer();
            if (server != null) {
                server.execute(() -> server.getPlayerList().getPlayers().forEach(
                        player -> server.getCommands().sendCommands(player)));
            }
            onClose();
        }

        @Override
        public void onClose() {
            minecraft.setScreenAndShow(parent);
        }
    }
}
