package dev.ftb.mods.ftblibrary;

import me.shedaniel.architectury.event.events.CommandRegistrationEvent;
import me.shedaniel.architectury.utils.Env;
import me.shedaniel.architectury.utils.EnvExecutor;

public class FTBLibrary {
	public static final String MOD_ID = "ftblibrary";

	public FTBLibrary() {
		EnvExecutor.runInEnv(Env.CLIENT, () -> () -> new FTBLibraryClient().init());

		// common events
		CommandRegistrationEvent.EVENT.register(FTBLibraryCommands::registerCommands);
	}
}