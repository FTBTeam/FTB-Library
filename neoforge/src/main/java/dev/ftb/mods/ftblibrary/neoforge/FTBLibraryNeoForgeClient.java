package dev.ftb.mods.ftblibrary.neoforge;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.client.FTBLibraryClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = FTBLibrary.MOD_ID, dist = Dist.CLIENT)
public class FTBLibraryNeoForgeClient {
    public FTBLibraryNeoForgeClient(IEventBus modEventBus) {
        var client = new FTBLibraryClient();

        // TODO: Events
    }
}
