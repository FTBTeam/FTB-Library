package com.feed_the_beast.mods.ftbguilibrary.fabric;

import com.feed_the_beast.mods.ftbguilibrary.FTBGUILibrary;
import net.fabricmc.api.ModInitializer;

public class FTBGUILibraryFabric implements ModInitializer
{
	@Override
	public void onInitialize()
	{
		new FTBGUILibrary();
	}
}
