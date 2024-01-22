package dev.ftb.mods.ftblibrary.icon;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;

/**
 * @author LatvianModder
 */
public interface Drawable {
	@Environment(EnvType.CLIENT)
	void draw(GuiGraphics graphics, int x, int y, int w, int h);

	@Environment(EnvType.CLIENT)
	default void drawStatic(GuiGraphics graphics, int x, int y, int w, int h) {
		draw(graphics, x, y, w, h);
	}

	@Environment(EnvType.CLIENT)
	default void draw3D(GuiGraphics graphics) {
		graphics.pose().pushPose();
		graphics.pose().scale(1F / 16F, 1F / 16F, 1F);
		draw(graphics, -8, -8, 16, 16);
		graphics.pose().popPose();
	}
}