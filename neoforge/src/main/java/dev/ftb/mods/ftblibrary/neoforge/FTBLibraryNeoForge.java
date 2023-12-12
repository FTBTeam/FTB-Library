package dev.ftb.mods.ftblibrary.neoforge;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import net.neoforged.fml.common.Mod;

@Mod(FTBLibrary.MOD_ID)
public class FTBLibraryNeoForge {
	public FTBLibraryNeoForge() {
		// EventBuses.registerModEventBus(FTBLibrary.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
		new FTBLibrary();
	}
}
