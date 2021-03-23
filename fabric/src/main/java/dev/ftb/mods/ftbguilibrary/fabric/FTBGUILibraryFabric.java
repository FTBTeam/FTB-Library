package dev.ftb.mods.ftbguilibrary.fabric;

import dev.ftb.mods.ftbguilibrary.FTBGUILibrary;
import net.fabricmc.api.ModInitializer;

public class FTBGUILibraryFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		new FTBGUILibrary();
	}
}
