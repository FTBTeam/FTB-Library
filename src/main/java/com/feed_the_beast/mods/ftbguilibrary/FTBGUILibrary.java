package com.feed_the_beast.mods.ftbguilibrary;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(FTBGUILibrary.MOD_ID)
public class FTBGUILibrary
{
	public static final String MOD_ID = "ftbguilibrary";
	public static final String MOD_NAME = "FTB GUI Library";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

	public FTBGUILibrary()
	{
		//FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
	}
}