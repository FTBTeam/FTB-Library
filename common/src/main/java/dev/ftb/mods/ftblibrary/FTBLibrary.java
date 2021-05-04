package dev.ftb.mods.ftblibrary;

import dev.ftb.mods.ftblibrary.net.FTBLibraryNet;
import me.shedaniel.architectury.event.events.CommandRegistrationEvent;
import me.shedaniel.architectury.utils.Env;
import me.shedaniel.architectury.utils.EnvExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FTBLibrary {
	public static final String MOD_ID = "ftblibrary";
	public static final String MOD_NAME = "FTB Library";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

	public FTBLibrary() {
		EnvExecutor.runInEnv(Env.CLIENT, () -> () -> new FTBLibraryClient().init());
		CommandRegistrationEvent.EVENT.register(FTBLibraryCommands::registerCommands);
		FTBLibraryNet.init();
	}
}