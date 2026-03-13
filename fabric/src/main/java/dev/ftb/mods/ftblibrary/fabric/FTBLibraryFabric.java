package dev.ftb.mods.ftblibrary.fabric;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.api.event.client.AllowChatCommandEvent;
import dev.ftb.mods.ftblibrary.api.event.client.CustomClickEvent;
import dev.ftb.mods.ftblibrary.api.event.client.RegisterCustomColorEvent;
import dev.ftb.mods.ftblibrary.api.event.client.SidebarButtonCreatedEvent;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.platform.event.EventPostingHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.network.chat.TextColor;

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
        EventPostingHandler.INSTANCE.registerEvent(SidebarButtonCreatedEvent.Data.class,
                data -> FTBLibraryFabricEvents.SIDEBAR_BUTTON_CREATED.invoker().buttonCreated(data));
        EventPostingHandler.INSTANCE.registerEventWithResult(AllowChatCommandEvent.Data.class, data -> ClientSendMessageEvents.ALLOW_COMMAND.invoker().allowSendCommandMessage(data.message()));

        // TODO test only, remove later
        FTBLibraryFabricEvents.REGISTER_CUSTOM_COLOR.register(event -> event.addColor("test", TextColor.fromRgb(0x8090A0B0)));
    }
}
