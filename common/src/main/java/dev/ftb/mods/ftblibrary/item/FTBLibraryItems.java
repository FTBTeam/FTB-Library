package dev.ftb.mods.ftblibrary.item;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.Registries;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class FTBLibraryItems {
	public static final Registrar<Item> REGISTRY = Registries.get(FTBLibrary.MOD_ID).get(net.minecraft.core.Registry.ITEM_REGISTRY);

	public static final Supplier<Item> FLUID_CONTAINER = REGISTRY.register(new ResourceLocation(FTBLibrary.MOD_ID, "fluid_container"), FTBLibraryItems::createFluidContainer);

	public static void init() {
	}

	@ExpectPlatform
	private static Item createFluidContainer() {
		throw new AssertionError();
	}
}
