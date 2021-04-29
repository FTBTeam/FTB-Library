package dev.ftb.mods.ftblibrary.forge;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.FTBLibraryClient;
import dev.ftb.mods.ftblibrary.sidebar.SidebarGroupGuiButton;
import dev.ftb.mods.ftblibrary.ui.IScreenWrapper;
import dev.ftb.mods.ftblibrary.util.WrappedIngredient;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

@JeiPlugin
public class JEIIntegration implements IModPlugin, IGlobalGuiHandler {
	@Override
	@NotNull
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(FTBLibrary.MOD_ID, "jei");
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGlobalGuiHandler(this);
	}

	@Override
	@NotNull
	public Collection<Rect2i> getGuiExtraAreas() {
		Screen currentScreen = Minecraft.getInstance().screen;

		if (FTBLibraryClient.areButtonsVisible(currentScreen)) {
			return Collections.singleton(SidebarGroupGuiButton.lastDrawnArea);
		}

		return Collections.emptySet();
	}

	@Override
	@Nullable
	public Object getIngredientUnderMouse(double mouseX, double mouseY) {
		Screen currentScreen = Minecraft.getInstance().screen;

		if (currentScreen instanceof IScreenWrapper) {
			return WrappedIngredient.unwrap(((IScreenWrapper) currentScreen).getGui().getIngredientUnderMouse());
		}

		return null;
	}
}