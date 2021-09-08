package dev.ftb.mods.ftblibrary;

import dev.ftb.mods.ftblibrary.item.FTBLibraryItems;
import dev.ftb.mods.ftblibrary.net.FTBLibraryNet;
import dev.ftb.mods.ftblibrary.net.SyncKnownServerRegistriesPacket;
import dev.ftb.mods.ftblibrary.util.KnownServerRegistries;
import me.shedaniel.architectury.event.events.CommandRegistrationEvent;
import me.shedaniel.architectury.event.events.LifecycleEvent;
import me.shedaniel.architectury.event.events.PlayerEvent;
import me.shedaniel.architectury.utils.EnvExecutor;
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
		FTBLibraryItems.init();
		PROXY.init();
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