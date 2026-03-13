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

@Mod(FTBLibrary.MOD_ID, dist = Dist.Client)
public class FTBLibraryNeoForgeClient {
    public FTBLibraryNeoForgeClient(IEventBus modEventBus) {
        client = new FTBLibraryClient();

        // TODO: Events
    }
}
