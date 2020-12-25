package com.feed_the_beast.mods.ftbguilibrary.fabric;

import com.feed_the_beast.mods.ftbguilibrary.FTBGUILibrary;
import net.fabricmc.api.ClientModInitializer;

public class FTBGUILibraryFabric implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		new FTBGUILibrary();
	}
}
