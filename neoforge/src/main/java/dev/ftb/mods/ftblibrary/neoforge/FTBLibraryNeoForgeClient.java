package dev.ftb.mods.ftblibrary.neoforge;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.api.event.client.AllowChatCommandEvent;
import dev.ftb.mods.ftblibrary.api.event.client.CustomClickEvent;
import dev.ftb.mods.ftblibrary.api.event.client.SidebarButtonCreatedEvent;
import dev.ftb.mods.ftblibrary.api.neoforge.FTBLibraryEvent;
import dev.ftb.mods.ftblibrary.client.FTBLibraryClient;
import dev.ftb.mods.ftblibrary.platform.event.NativeEventPosting;
import dev.ftb.mods.ftblibrary.util.neoforge.NeoEventHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = FTBLibrary.MOD_ID, dist = Dist.CLIENT)
public class FTBLibraryNeoForgeClient {
    public FTBLibraryNeoForgeClient(IEventBus modEventBus) {
        var client = new FTBLibraryClient();

        IEventBus bus = NeoForge.EVENT_BUS;

        bus.addListener(ClientStartedEvent.class, event -> client.onClientStarted(event.getClient()));
        bus.addListener(ScreenEvent.Init.Post.class, event -> client.guiInit(event.getScreen()));
        bus.addListener(ClientTickEvent.Post.class, ignored -> client.clientTick());
        bus.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> client.onPlayerLogout(event.getPlayer()));

        registerNeoEventPosters(bus);

        bus.addListener(FTBLibraryEvent.SidebarButtonCreated.class, event ->
                client.addVisibilityConditionToSidebarButton(event.getButton()));
    }

    private static void registerNeoEventPosters(IEventBus bus) {
        NeoEventHelper.registerNeoEventPoster(bus, SidebarButtonCreatedEvent.Data.class, FTBLibraryEvent.SidebarButtonCreated::new);

        NeoEventHelper.registerCancellableNeoEventPoster(bus, CustomClickEvent.TYPE, FTBLibraryEvent.CustomClick::new);

        NativeEventPosting.INSTANCE.registerEventWithResult(AllowChatCommandEvent.TYPE, data ->
                !ClientHooks.onClientSendMessage(data.message()).isEmpty());
    }
}
