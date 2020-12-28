package com.feed_the_beast.mods.ftbguilibrary.fabric;

import com.feed_the_beast.mods.ftbguilibrary.FTBGUILibrary;
import com.feed_the_beast.mods.ftbguilibrary.sidebar.GuiButtonSidebarGroup;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;

public class REIIntegration implements REIPluginV0
{
	@Override
	public ResourceLocation getPluginIdentifier()
	{
		return new ResourceLocation(FTBGUILibrary.MOD_ID, "rei");
	}

	@Override
	public void registerBounds(DisplayHelper displayHelper)
	{
		displayHelper.getBaseBoundsHandler().registerExclusionZones(Screen.class, () -> {
			Screen currentScreen = Minecraft.getInstance().screen;

			if (FTBGUILibrary.areButtonsVisible(currentScreen))
			{
				Rect2i area = GuiButtonSidebarGroup.lastDrawnArea;
				return Collections.singletonList(new Rectangle(area.getX(), area.getY(), area.getWidth(), area.getHeight()));
			}

			return Collections.emptyList();
		});
	}
}
