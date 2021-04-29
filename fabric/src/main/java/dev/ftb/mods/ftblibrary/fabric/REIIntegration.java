package dev.ftb.mods.ftblibrary.fabric;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.FTBLibraryClient;
import dev.ftb.mods.ftblibrary.sidebar.SidebarGroupGuiButton;
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
		return new ResourceLocation(FTBLibrary.MOD_ID, "rei");
	}

	@Override
	public void registerBounds(DisplayHelper displayHelper) {
		displayHelper.getBaseBoundsHandler().registerExclusionZones(Screen.class, () -> {
			Screen currentScreen = Minecraft.getInstance().screen;

			if (FTBLibraryClient.areButtonsVisible(currentScreen)) {
				Rect2i area = SidebarGroupGuiButton.lastDrawnArea;
				return Collections.singletonList(new Rectangle(area.getX(), area.getY(), area.getWidth(), area.getHeight()));
			}

			return Collections.emptyList();
		});
	}
}
