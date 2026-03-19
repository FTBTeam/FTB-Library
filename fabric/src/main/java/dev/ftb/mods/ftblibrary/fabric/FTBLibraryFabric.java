package dev.ftb.mods.ftblibrary.fabric;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.api.event.client.CustomClickEvent;
import dev.ftb.mods.ftblibrary.api.event.client.RegisterCustomColorEvent;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.util.fabric.FabricEventHelper;
import net.fabricmc.api.ModInitializer;
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
        ServerPlayerEvents.JOIN.register(library::playerJoined);

        CommandRegistrationCallback.EVENT.register(library::registerCommands);

        FabricEventHelper.registerFabricEventPoster(RegisterCustomColorEvent.Data.class, FTBLibraryFabricEvents.REGISTER_CUSTOM_COLOR);
    }
}
