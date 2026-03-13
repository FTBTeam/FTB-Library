package dev.ftb.mods.ftblibrary.fabric;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManagerClient;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class FTBLibraryFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        var library = new FTBLibrary();

        ServerLifecycleEvents.SERVER_STARTING.register(ConfigManager.getInstance()::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(library::serverStarted);
        ServerLifecycleEvents.SERVER_STOPPED.register(library::serverStopped);
        ServerPlayerEvents.JOIN.register((player) -> {
            library.playerJoined(player);
            ConfigManager.getInstance().onPlayerLogin(player);
        });

        CommandRegistrationCallback.EVENT.register(library::registerCommands);
        ClientLifecycleEvents.CLIENT_STARTED.register(ConfigManagerClient::onClientStarted);
    }
}
