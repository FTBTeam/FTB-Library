package dev.ftb.mods.ftblibrary.fabric;

import dev.ftb.mods.ftblibrary.FTBLibraryClient;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectImageResourceScreen;
import dev.ftb.mods.ftblibrary.icon.EntityIconLoader;
import dev.ftb.mods.ftblibrary.sidebar.SidebarButtonManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.server.packs.PackType;

public class FTBLibraryFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        var client = new FTBLibraryClient();

        ClientLifecycleEvents.CLIENT_STARTED.register(client::onClientStarted);

        ScreenEvents.AFTER_INIT.register((mc, screen, scaledWidth, scaledHeight) ->
                client.guiInit(screen));
        ClientTickEvents.END_CLIENT_TICK.register(client::clientTick);
        // TODO: Validate: This might be the right event for ClientPlayerEvent.CLIENT_PLAYER_QUIT
        ClientPlayConnectionEvents.DISCONNECT.register((listener, mc) -> {
            client.onPlayerLogout(mc.player);
        });

        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(FTBLibraryClient.SIDEBAR_LISTENER, SidebarButtonManager.INSTANCE);
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(FTBLibraryClient.IMAGE_SELECT_LISTENER, SelectImageResourceScreen.ResourceListener.INSTANCE);
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(FTBLibraryClient.ENTITY_ICON_LISTENER, new EntityIconLoader());
    }
}
