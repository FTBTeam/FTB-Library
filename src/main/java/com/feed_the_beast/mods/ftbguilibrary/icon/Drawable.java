package com.feed_the_beast.mods.ftbguilibrary.icon;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author LatvianModder
 */
public interface Drawable
{
	@OnlyIn(Dist.CLIENT)
	void draw(int x, int y, int w, int h);

	@OnlyIn(Dist.CLIENT)
	default void drawStatic(int x, int y, int w, int h)
	{
		draw(x, y, w, h);
	}

	@OnlyIn(Dist.CLIENT)
	default void draw3D(MatrixStack matrixStack)
	{
		RenderSystem.pushMatrix();
		RenderSystem.scaled(1D / 16D, 1D / 16D, 1D);
		draw(-8, -8, 16, 16);
		RenderSystem.popMatrix();
	}
}