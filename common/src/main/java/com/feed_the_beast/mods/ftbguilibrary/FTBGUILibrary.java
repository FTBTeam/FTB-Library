package com.feed_the_beast.mods.ftbguilibrary;

import me.shedaniel.architectury.event.events.CommandRegistrationEvent;
import me.shedaniel.architectury.platform.Platform;
import me.shedaniel.architectury.utils.Env;
import me.shedaniel.architectury.utils.EnvExecutor;

public class FTBGUILibrary
{
	public static final String MOD_ID = "ftbguilibrary";

	public FTBGUILibrary()
	{
		EnvExecutor.runInEnv(Env.CLIENT, () -> () -> new FTBGUILibraryClient().init());

		// common events
		CommandRegistrationEvent.EVENT.register(FTBGUILibraryCommands::registerCommands);
	}
}