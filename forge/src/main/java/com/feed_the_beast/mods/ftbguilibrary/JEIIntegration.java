package com.feed_the_beast.mods.ftbguilibrary;

import com.feed_the_beast.mods.ftbguilibrary.sidebar.GuiButtonSidebarGroup;
import com.feed_the_beast.mods.ftbguilibrary.widget.IGuiWrapper;
import com.feed_the_beast.mods.ftbguilibrary.widget.WrappedIngredient;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

@JeiPlugin
public class JEIIntegration implements IModPlugin, IGlobalGuiHandler
{
	@Override
	public ResourceLocation getPluginUid()
	{
		return new ResourceLocation(FTBGUILibrary.MOD_ID, "jei");
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration)
	{
		registration.addGlobalGuiHandler(this);
	}

	@Override
	public Collection<Rect2i> getGuiExtraAreas()
	{
		Screen currentScreen = Minecraft.getInstance().screen;

		if (FTBGUILibrary.areButtonsVisible(currentScreen))
		{
			return Collections.singleton(GuiButtonSidebarGroup.lastDrawnArea);
		}

		return Collections.emptySet();
	}

	@Override
	@Nullable
	public Object getIngredientUnderMouse(double mouseX, double mouseY)
	{
		Screen currentScreen = Minecraft.getInstance().screen;

		if (currentScreen instanceof IGuiWrapper)
		{
			return WrappedIngredient.unwrap(((IGuiWrapper) currentScreen).getGui().getIngredientUnderMouse());
		}

		return null;
	}
}