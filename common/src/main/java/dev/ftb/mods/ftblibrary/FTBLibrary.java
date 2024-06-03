package dev.ftb.mods.ftblibrary;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import dev.ftb.mods.ftblibrary.net.FTBLibraryNet;
import dev.ftb.mods.ftblibrary.net.SyncKnownServerRegistriesPacket;
import dev.ftb.mods.ftblibrary.util.KnownServerRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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

		EnvExecutor.runInEnv(Env.CLIENT, () -> FTBLibraryClient::init);
	}

	public static ResourceLocation rl(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

	private void serverStarted(MinecraftServer server) {
		KnownServerRegistries.server = KnownServerRegistries.create(server);
	}

	private void serverStopped(MinecraftServer server) {
		KnownServerRegistries.server = null;
	}

	private void playerJoined(ServerPlayer player) {
		NetworkManager.sendToPlayer(player, new SyncKnownServerRegistriesPacket(KnownServerRegistries.server));
	}
}