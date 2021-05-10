package dev.ftb.mods.ftblibrary.fabric;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import net.fabricmc.api.ModInitializer;

public class FTBLibraryFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		new FTBLibrary();
	}
}
