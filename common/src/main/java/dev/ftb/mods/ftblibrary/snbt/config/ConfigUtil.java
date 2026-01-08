package dev.ftb.mods.ftblibrary.snbt.config;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftblibrary.client.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.gui.EditConfigScreen;
import net.minecraft.util.Util;
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
     * {@link EditConfigScreen} constructor.
     *
     * @param config the config to be edited
     * @param groupName the config group name, which is the top-level path for objects in this group (used primarily for translations)
     * @param isServerConfig if true, config is sync'd to server after editing; if false, config is saved locally on client
     * @return a new config group object
     */
    static ConfigGroup makeConfigEditGroup(SNBTConfig config, String groupName, boolean isServerConfig) {
        return Util.make(ConfigGroup.createEditable(config, groupName, isServerConfig), config::fillClientConfig);
    }
}
