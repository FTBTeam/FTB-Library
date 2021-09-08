package dev.ftb.mods.ftblibrary.forge;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import me.shedaniel.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FTBLibrary.MOD_ID)
public class FTBLibraryForge {
	public FTBLibraryForge() {
		EventBuses.registerModEventBus(FTBLibrary.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
		new FTBLibrary();
	}
}
