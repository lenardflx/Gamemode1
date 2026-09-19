package dev.gamemode1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger("Gamemode1");
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("gamemode1.json");

    public static final ModConfig INSTANCE = load();

    public boolean enableGamemode = true;
    public boolean enableGm = true;
    public boolean enableDefaultGamemode = true;
    public boolean enableDifficulty = true;
    public boolean enableWeather = true;
    public boolean enableExperience = true;
    public boolean enableLocate = true;
    public boolean enableHelp = true;
    public boolean enableReplaceItem = true;
    public boolean enableGamemodeShortcuts = true;

    private static ModConfig load() {
        if (!Files.exists(FILE)) {
            ModConfig config = new ModConfig();
            config.save();
            return config;
        }
        try (Reader reader = Files.newBufferedReader(FILE)) {
            ModConfig config = GSON.fromJson(reader, ModConfig.class);
            if (config != null) {
                return config;
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Could not read {}; using defaults", FILE, e);
        }
        return new ModConfig();
    }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this));
        } catch (IOException e) {
            LOGGER.error("Could not save {}", FILE, e);
        }
    }
}
