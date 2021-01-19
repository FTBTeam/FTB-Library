package com.feed_the_beast.mods.ftbguilibrary;

import me.shedaniel.architectury.platform.Platform;
import me.shedaniel.architectury.utils.Env;

public class FTBGUILibrary
{
	public static final String MOD_ID = "ftbguilibrary";

	public FTBGUILibrary()
	{
		if (Platform.getEnvironment() == Env.CLIENT)
		{
			initClient();
		}
	}

	private void initClient()
	{
		new FTBGUILibraryClient().init();
	}
}