package dev.ftb.mods.ftblibrary.fabric;

import dev.ftb.mods.ftblibrary.api.event.client.AllowChatCommandEvent;
import dev.ftb.mods.ftblibrary.api.event.client.CustomClickEvent;
import dev.ftb.mods.ftblibrary.api.event.client.SidebarButtonCreatedEvent;
import dev.ftb.mods.ftblibrary.client.FTBLibraryClient;
import dev.ftb.mods.ftblibrary.platform.event.NativeEventPosting;
import dev.ftb.mods.ftblibrary.util.fabric.FabricEventHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

public class FTBLibraryFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        var client = new FTBLibraryClient();

        ClientLifecycleEvents.CLIENT_STARTED.register(client::onClientStarted);

        ScreenEvents.AFTER_INIT.register((mc, screen, scaledWidth, scaledHeight) ->
                client.guiInit(screen));

        ClientTickEvents.END_CLIENT_TICK.register(ignored -> client.clientTick());
        // TODO: Validate: This might be the right event for ClientPlayerEvent.CLIENT_PLAYER_QUIT
        ClientPlayConnectionEvents.DISCONNECT.register((listener, mc) -> {
            client.onPlayerLogout(mc.player);
        });

        registerFabricEventPosters();

        FTBLibraryFabricEvents.SIDEBAR_BUTTON_CREATED.register(data -> client.addVisibilityConditionToSidebarButton(data.button()));
    }

    private static void registerFabricEventPosters() {
        FabricEventHelper.registerFabricEventPoster(SidebarButtonCreatedEvent.Data.class, FTBLibraryFabricEvents.SIDEBAR_BUTTON_CREATED);

        FabricEventHelper.registerFabricEventPosterPredicate(CustomClickEvent.TYPE, FTBLibraryFabricEvents.CUSTOM_CLICK);

        NativeEventPosting.INSTANCE.registerEventWithResult(
                AllowChatCommandEvent.TYPE,
                data -> ClientSendMessageEvents.ALLOW_COMMAND.invoker().allowSendCommandMessage(data.message())
        );
    }
}
