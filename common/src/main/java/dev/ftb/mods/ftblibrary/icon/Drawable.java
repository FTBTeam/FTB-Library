package dev.ftb.mods.ftblibrary.icon;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * @author LatvianModder
 */
public interface Drawable {
	@Environment(EnvType.CLIENT)
	void draw(PoseStack matrixStack, int x, int y, int w, int h);

	@Environment(EnvType.CLIENT)
	default void drawStatic(PoseStack matrixStack, int x, int y, int w, int h) {
		draw(matrixStack, x, y, w, h);
	}

	@Environment(EnvType.CLIENT)
	default void draw3D(PoseStack matrixStack) {
		matrixStack.pushPose();
		matrixStack.scale(1F / 16F, 1F / 16F, 1F);
		draw(matrixStack, -8, -8, 16, 16);
		matrixStack.popPose();
	}
}