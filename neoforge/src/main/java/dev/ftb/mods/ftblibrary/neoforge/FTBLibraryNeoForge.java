package dev.ftb.mods.ftblibrary.neoforge;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod(FTBLibrary.MOD_ID)
public class FTBLibraryNeoForge {
    private final FTBLibrary library;

    public FTBLibraryNeoForge(IEventBus modEventBus) {
        this.library = new FTBLibrary();

        modEventBus.addListener(FMLCommonSetupEvent.class, (_) -> this.library.onSetup());
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, (event) -> this.library.serverStarted(event.getServer()));
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, (event) -> this.library.serverStarted(event.getServer()));
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, (event) -> this.library.registerCommands(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection()));
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, (event) -> {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                this.library.playerJoined(serverPlayer);
            }
        });
    }
}
