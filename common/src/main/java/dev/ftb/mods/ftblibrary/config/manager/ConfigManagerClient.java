package dev.ftb.mods.ftblibrary.config.manager;

import dev.ftb.mods.ftblibrary.client.config.gui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.config.ConfigUtil;
import net.minecraft.client.Minecraft;

public class ConfigManagerClient {
    public static void onClientStarted(Minecraft minecraft) {
        ConfigManager mgr = ConfigManager.getInstance();

        mgr.pendingClient.forEach((key, config) -> {
            mgr.findAndLoad(key, config, ConfigUtil.LOCAL_DIR::resolve);
        });

        // all synced server configs are also registered on the client (but not saved there!)
        mgr.pendingServer.forEach((key, config) -> {
            if (config.synced()) {
                mgr.startTracking(key, config.clientMirrorOfServerConfig());
            }
        });
    }

    public static void editConfig(String configName) {
        editConfig(configName, false);
    }

    public static void editConfig(String configName, boolean isReadOnly) {
        ConfigManager.getInstance().createConfigGroup(configName)
                .ifPresent(group -> new EditConfigScreen(group, isReadOnly).setAutoclose(true).openGui());
    }
}
