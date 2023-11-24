package dev.ftb.mods.ftblibrary.forge;

import dev.architectury.platform.hooks.EventBusesHooks;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FTBLibrary.MOD_ID)
public class FTBLibraryForge {
	public FTBLibraryForge() {
		EventBusesHooks.whenAvailable(FTBLibrary.MOD_ID, eventBus -> eventBus.register(FMLJavaModLoadingContext.get().getModEventBus()));
		new FTBLibrary();
	}
}
