package dev.ftb.mods.ftblibrary.fabric;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class FTBLibraryFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        var library = new FTBLibrary();

        ServerLifecycleEvents.SERVER_STARTED.register(library::serverStarted);
        ServerLifecycleEvents.SERVER_STOPPED.register(library::serverStopped);
        ServerPlayerEvents.JOIN.register(library::playerJoined);

        CommandRegistrationCallback.EVENT.register(library::registerCommands);
        ClientSendMessageEvents.CHAT.invoker().onSendChatMessage("command");

        // TODO: Setup event
    }
}
