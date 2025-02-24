package dev.ftb.mods.ftblibrary.snbt.config;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import net.minecraft.Util;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

public interface ConfigUtil {
    Path ROOT_DIR = Platform.getGameFolder();

    Path DEFAULT_CONFIG_DIR = ROOT_DIR.resolve("defaultconfigs");
    Path CONFIG_DIR = Platform.getConfigFolder();
    Path LOCAL_DIR = ROOT_DIR.resolve("local");

    LevelResource SERVER_CONFIG_DIR = new LevelResource("serverconfig");

    /**
     * Create a {@link ConfigGroup} object, suitable for passing as a parameter to the
     * {@link dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen} constructor.
     *
     * @param config the config to be edited
     * @param groupName the config group name, which is the top-level path for objects in this group (used primarily for translations)
     * @param isServerConfig if true, config is sync'd to server after editing; if false, config is saved locally on client
     * @return a new config group object
     */
    static ConfigGroup makeConfigEditGroup(SNBTConfig config, String groupName, boolean isServerConfig) {
        return Util.make(ConfigGroup.createEditable(config, groupName, isServerConfig), config::createClientConfig);
    }

    /**
     * @see #loadDefaulted(SNBTConfig, Path, String, String)
     * @deprecated use {@link dev.ftb.mods.ftblibrary.config.manager.ConfigManager} now
     */
    @Deprecated(forRemoval = true)
    static void loadDefaulted(SNBTConfig config, Path configDir, String namespace) {
        loadDefaulted(config, configDir, namespace, config.key + ".snbt");
    }

    /**
     * Utility method to load a config file, with an optional default file
     * containing instructions on how to set up configuration defaults.
     *
     * @param config    Configuration object the file will be loaded into
     * @param configDir Directory containing the actual config file
     * @param namespace Namespace the default config files will be stored under
     *                  (most likely the same as the mod ID)
     * @param filename  Filename of the configuration file
     * @deprecated use {@link dev.ftb.mods.ftblibrary.config.manager.ConfigManager} now
     */
    @Deprecated(forRemoval = true)
    static void loadDefaulted(SNBTConfig config, Path configDir, String namespace, String filename) {
        var configPath = configDir.resolve(filename).toAbsolutePath();
        var defaultPath = DEFAULT_CONFIG_DIR.resolve(namespace).resolve(filename);
        config.load(
                configPath,
                defaultPath,
                () -> new String[]{
                        "Default config file that will be copied to " + ROOT_DIR.relativize(configPath) + " if it doesn't exist!",
                        "Just copy any values you wish to override in here!",
                }
        );

    }
}
