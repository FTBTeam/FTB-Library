package dev.ftb.mods.ftblibrary;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.registries.DeferredSupplier;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import dev.ftb.mods.ftblibrary.items.ModItems;
import dev.ftb.mods.ftblibrary.net.FTBLibraryNet;
import dev.ftb.mods.ftblibrary.net.SyncKnownServerRegistriesPacket;
import dev.ftb.mods.ftblibrary.util.KnownServerRegistries;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FTBLibrary {
    public static final String MOD_ID = "ftblibrary";
    public static final String MOD_NAME = "FTB Library";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public FTBLibrary() {
        CommandRegistrationEvent.EVENT.register(FTBLibraryCommands::registerCommands);
        FTBLibraryNet.register();
        LifecycleEvent.SERVER_STARTED.register(this::serverStarted);
        LifecycleEvent.SERVER_STOPPED.register(this::serverStopped);
        PlayerEvent.PLAYER_JOIN.register(this::playerJoined);

        ModItems.init();

        EnvExecutor.runInEnv(Env.CLIENT, () -> FTBLibraryClient::init);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static DeferredSupplier<CreativeModeTab> getCreativeModeTab() {
        return ModItems.FTB_LIBRARY_TAB;
    }

    private void serverStarted(MinecraftServer server) {
        KnownServerRegistries.server = KnownServerRegistries.create(server);
    }

    private void serverStopped(MinecraftServer server) {
        KnownServerRegistries.server = null;
    }

    private void playerJoined(ServerPlayer player) {
        if (KnownServerRegistries.server != null) {
            // can be null, e.g. https://github.com/FTBTeam/FTB-Mods-Issues/issues/1387
            NetworkHelper.sendTo(player, new SyncKnownServerRegistriesPacket(KnownServerRegistries.server));
        }
    }
}
