package dev.ftb.mods.ftblibrary.neoforge;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.api.event.client.CustomClickEvent;
import dev.ftb.mods.ftblibrary.api.event.client.RegisterCustomColorEvent;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.neoforge.platform.networking.NeoNetworkRegistryImpl;
import dev.ftb.mods.ftblibrary.platform.Platform;
import dev.ftb.mods.ftblibrary.platform.event.EventPostingHandler;
import dev.ftb.mods.ftblibrary.platform.network.Networking;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(FTBLibrary.MOD_ID)
public class FTBLibraryNeoForge {
    private final FTBLibrary library;

    public FTBLibraryNeoForge(IEventBus modEventBus) {
        this.library = new FTBLibrary();

        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, (event) -> this.library.serverStarted(event.getServer()));
        NeoForge.EVENT_BUS.addListener(ServerStartingEvent.class, (event) -> ConfigManager.getInstance().onServerStarting(event.getServer()));
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, (event) -> this.library.serverStopped(event.getServer()));

        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, (event) -> this.library.registerCommands(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection()));
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, (event) -> {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                this.library.playerJoined(serverPlayer);
                ConfigManager.getInstance().onPlayerLogin(serverPlayer);
            }
        });

        EventPostingHandler.INSTANCE.registerEventWithResult(CustomClickEvent.Data.class, data -> NeoForge.EVENT_BUS.post(new FTBLibraryNeoForgeEvents.CustomClickEvent(data.id())).isCanceled());
        EventPostingHandler.INSTANCE.registerEvent(RegisterCustomColorEvent.Data.class, data -> NeoForge.EVENT_BUS.post(new FTBLibraryNeoForgeEvents.RegisterCustomColorEvent(data.colors())));

        modEventBus.register(this);
    }

    @SubscribeEvent // on the mod event bus
    public void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        Networking networking = Platform.get().networking();
        ((NeoNetworkRegistryImpl) networking.registry()).collectPackets(registrar);
    }
}
