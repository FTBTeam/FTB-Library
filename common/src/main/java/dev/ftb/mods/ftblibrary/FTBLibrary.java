package dev.ftb.mods.ftblibrary;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
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
	public static FTBLibraryCommon PROXY;

	public FTBLibrary() {
		PROXY = EnvExecutor.getEnvSpecific(() -> FTBLibraryClient::new, () -> FTBLibraryCommon::new);
		CommandRegistrationEvent.EVENT.register(FTBLibraryCommands::registerCommands);
		FTBLibraryNet.init();
		LifecycleEvent.SERVER_STARTED.register(this::serverStarted);
		LifecycleEvent.SERVER_STOPPED.register(this::serverStopped);
		PlayerEvent.PLAYER_JOIN.register(this::playerJoined);
		PROXY.init();
	}

	public static ResourceLocation rl(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

	private void serverStarted(MinecraftServer server) {
		KnownServerRegistries.server = new KnownServerRegistries(server);
	}

	private void serverStopped(MinecraftServer server) {
		KnownServerRegistries.server = null;
	}

	private void playerJoined(ServerPlayer player) {
		new SyncKnownServerRegistriesPacket(KnownServerRegistries.server).sendTo(player);
	}
}