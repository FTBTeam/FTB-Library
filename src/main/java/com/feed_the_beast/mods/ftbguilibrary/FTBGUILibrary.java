package com.feed_the_beast.mods.ftbguilibrary;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(FTBGUILibrary.MOD_ID)
public class FTBGUILibrary
{
	public static final String MOD_ID = "ftbguilibrary";

	public FTBGUILibrary()
	{
		//noinspection Convert2MethodRef
		DistExecutor.runForDist(() -> () -> new FTBGUILibraryClient(), () -> () -> new FTBGUILibraryCommon()).init();
	}
}