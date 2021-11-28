package dev.ftb.mods.ftblibrary.snbt.config;

import dev.architectury.hooks.LevelResourceHooks;
import dev.architectury.platform.Platform;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

public interface ConfigUtil {
	Path ROOT_DIR = Platform.getGameFolder();

	Path DEFAULT_CONFIG_DIR = ROOT_DIR.resolve("defaultconfigs");
	Path CONFIG_DIR = Platform.getConfigFolder();
	Path LOCAL_DIR = ROOT_DIR.resolve("local");

	LevelResource SERVER_CONFIG_DIR = LevelResourceHooks.create("serverconfig");

	/**
	 * @see #loadDefaulted(SNBTConfig, Path, String, String)
	 */
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
	 */
	static void loadDefaulted(SNBTConfig config, Path configDir, String namespace, String filename) {
		Path configPath = configDir.resolve(filename).toAbsolutePath();
		Path defaultPath = DEFAULT_CONFIG_DIR.resolve(namespace).resolve(filename);
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
