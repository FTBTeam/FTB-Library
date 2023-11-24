package dev.ftb.mods.ftblibrary.forge;

import dev.architectury.platform.hooks.EventBusesHooks;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FTBLibrary.MOD_ID)
public class FTBLibraryNeoForge {
	public FTBLibraryNeoForge() {
		// EventBuses.registerModEventBus(FTBLibrary.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
		new FTBLibrary();
	}
}
