package dev.ftb.mods.ftblibrary.fabric;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.api.event.client.CustomClickEvent;
import dev.ftb.mods.ftblibrary.api.event.client.RegisterCustomColorEvent;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.platform.event.EventPostingHandler;
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
        ServerPlayerEvents.JOIN.register((player) -> {
            library.playerJoined(player);
            ConfigManager.getInstance().onPlayerLogin(player);
        });

        CommandRegistrationCallback.EVENT.register(library::registerCommands);

        EventPostingHandler.INSTANCE.registerEventWithResult(CustomClickEvent.Data.class,
                data -> FTBLibraryFabricEvents.CUSTOM_CLICK.invoker().onClicked(data));
        EventPostingHandler.INSTANCE.registerEvent(RegisterCustomColorEvent.Data.class,
                data -> FTBLibraryFabricEvents.REGISTER_CUSTOM_COLOR.invoker().register(data));
    }
}
