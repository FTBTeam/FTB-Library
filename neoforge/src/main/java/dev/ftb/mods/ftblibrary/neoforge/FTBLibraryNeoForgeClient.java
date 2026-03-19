package dev.ftb.mods.ftblibrary.neoforge;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.api.neoforge.FTBLibraryEvent;
import dev.ftb.mods.ftblibrary.api.event.client.AllowChatCommandEvent;
import dev.ftb.mods.ftblibrary.api.event.client.SidebarButtonCreatedEvent;
import dev.ftb.mods.ftblibrary.client.FTBLibraryClient;
import dev.ftb.mods.ftblibrary.platform.event.EventPostingHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = FTBLibrary.MOD_ID, dist = Dist.CLIENT)
public class FTBLibraryNeoForgeClient {
    public FTBLibraryNeoForgeClient(IEventBus modEventBus) {
        var client = new FTBLibraryClient();

        NeoForge.EVENT_BUS.addListener(ClientStartedEvent.class, event -> client.onClientStarted(event.getClient()));
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Init.Post.class, event -> client.guiInit(event.getScreen()));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, ignored -> client.clientTick());
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> client.onPlayerLogout(event.getPlayer()));

        EventPostingHandler.INSTANCE.registerEvent(SidebarButtonCreatedEvent.Data.class,
                data -> NeoForge.EVENT_BUS.post(new FTBLibraryEvent.SidebarButtonCreated(data)));
        EventPostingHandler.INSTANCE.registerEventWithResult(AllowChatCommandEvent.Data.class,
                data -> !ClientHooks.onClientSendMessage(data.message()).isEmpty());

        NeoForge.EVENT_BUS.addListener(FTBLibraryEvent.SidebarButtonCreated.class, event ->
                client.addVisibilityConditionToSidebarButton(event.getButton()));
    }
}
