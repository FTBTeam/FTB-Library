package dev.ftb.mods.ftbguilibrary;

import me.shedaniel.architectury.event.events.CommandRegistrationEvent;
import me.shedaniel.architectury.utils.Env;
import me.shedaniel.architectury.utils.EnvExecutor;

public class FTBGUILibrary {
	public static final String MOD_ID = "ftbguilibrary";

	public FTBGUILibrary() {
		EnvExecutor.runInEnv(Env.CLIENT, () -> () -> new FTBGUILibraryClient().init());

		// common events
		CommandRegistrationEvent.EVENT.register(FTBGUILibraryCommands::registerCommands);
	}
}