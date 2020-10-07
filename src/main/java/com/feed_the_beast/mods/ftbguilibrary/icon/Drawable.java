package com.feed_the_beast.mods.ftbguilibrary.icon;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author LatvianModder
 */
public interface Drawable
{
	@OnlyIn(Dist.CLIENT)
	void draw(MatrixStack matrixStack, int x, int y, int w, int h);

	@OnlyIn(Dist.CLIENT)
	default void drawStatic(MatrixStack matrixStack, int x, int y, int w, int h)
	{
		draw(matrixStack, x, y, w, h);
	}

	@OnlyIn(Dist.CLIENT)
	default void draw3D(MatrixStack matrixStack)
	{
		matrixStack.push();
		matrixStack.scale(1F / 16F, 1F / 16F, 1F);
		draw(matrixStack, -8, -8, 16, 16);
		matrixStack.pop();
	}
}