package dev.ftb.mods.ftblibrary.neoforge;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.api.event.client.RegisterCustomColorEvent;
import dev.ftb.mods.ftblibrary.api.neoforge.FTBLibraryEvent;
import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.items.ModItems;
import dev.ftb.mods.ftblibrary.neoforge.platform.networking.NeoNetworkRegistryImpl;
import dev.ftb.mods.ftblibrary.platform.Platform;
import dev.ftb.mods.ftblibrary.platform.network.Networking;
import dev.ftb.mods.ftblibrary.util.neoforge.NeoEventHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
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

        IEventBus bus = NeoForge.EVENT_BUS;

        bus.addListener(ServerStartedEvent.class, (event) -> this.library.serverStarted(event.getServer()));
        bus.addListener(ServerStartingEvent.class, (event) -> ConfigManager.getInstance().onServerStarting(event.getServer()));
        bus.addListener(ServerStoppedEvent.class, (event) -> this.library.serverStopped(event.getServer()));

        bus.addListener(RegisterCommandsEvent.class, (event) -> this.library.registerCommands(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection()));
        bus.addListener(PlayerEvent.PlayerLoggedInEvent.class, (event) -> {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                this.library.playerJoined(serverPlayer);
            }
        });

        modEventBus.addListener(BuildCreativeModeTabContentsEvent.class, event -> {
            if (event.getTab() == FTBLibrary.getCreativeModeTab().get()) {
                event.accept(ModItems.ICON_ITEM.get());
            }
        });

        NeoEventHelper.registerNeoEventPoster(bus, RegisterCustomColorEvent.Data.class, FTBLibraryEvent.RegisterCustomColor::new);

        modEventBus.register(this);
    }

    @SubscribeEvent // on the mod event bus
    public void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        Networking networking = Platform.get().networking();
        ((NeoNetworkRegistryImpl) networking.registry()).collectPackets(registrar);
    }

}
