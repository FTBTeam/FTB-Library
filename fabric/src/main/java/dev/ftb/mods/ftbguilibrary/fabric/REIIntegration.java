package dev.ftb.mods.ftbguilibrary.fabric;

import dev.ftb.mods.ftbguilibrary.FTBGUILibrary;
import dev.ftb.mods.ftbguilibrary.FTBGUILibraryClient;
import dev.ftb.mods.ftbguilibrary.sidebar.SidebarGroupGuiButton;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.DisplayHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;

public class REIIntegration implements REIPluginV0 {
	@Override
	public ResourceLocation getPluginIdentifier() {
		return new ResourceLocation(FTBGUILibrary.MOD_ID, "rei");
	}

	@Override
	public void registerBounds(DisplayHelper displayHelper) {
		displayHelper.getBaseBoundsHandler().registerExclusionZones(Screen.class, () -> {
			Screen currentScreen = Minecraft.getInstance().screen;

			if (FTBGUILibraryClient.areButtonsVisible(currentScreen)) {
				Rect2i area = SidebarGroupGuiButton.lastDrawnArea;
				return Collections.singletonList(new Rectangle(area.getX(), area.getY(), area.getWidth(), area.getHeight()));
			}

			return Collections.emptyList();
		});
	}
}
