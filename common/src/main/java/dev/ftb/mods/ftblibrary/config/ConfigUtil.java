package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.gui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.config.value.Config;
import dev.ftb.mods.ftblibrary.platform.Platform;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

public interface ConfigUtil {
    Path ROOT_DIR = Platform.get().paths().gamePath();

    Path DEFAULT_CONFIG_DIR = ROOT_DIR.resolve("defaultconfigs");
    Path CONFIG_DIR = Platform.get().paths().configPath();
    Path LOCAL_DIR = ROOT_DIR.resolve("local");

    LevelResource SERVER_CONFIG_DIR = new LevelResource("serverconfig");

    /**
     * Create a {@link EditableConfigGroup} object, suitable for passing as a parameter to the
     * {@link EditConfigScreen} constructor.
     *
     * @param config the config to be edited
     * @param groupName the config group name, which is the top-level path for objects in this group (used primarily for translations)
     * @param isServerConfig if true, config is sync'd to server after editing; if false, config is saved locally on client
     * @return a new config group object
     */
    static EditableConfigGroup makeConfigEditGroup(Config config, String groupName, boolean isServerConfig) {
        return Util.make(EditableConfigGroup.createEditable(config, groupName, isServerConfig), config::addToEditableConfigGroup);
    }
}
