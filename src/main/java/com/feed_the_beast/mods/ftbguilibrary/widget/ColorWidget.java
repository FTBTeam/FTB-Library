package com.feed_the_beast.mods.ftbguilibrary.widget;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.mojang.blaze3d.matrix.MatrixStack;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class ColorWidget extends Widget
{
	public final Color4I color;
	public Color4I mouseOverColor;

	public ColorWidget(Panel panel, Color4I c, @Nullable Color4I m)
	{
		super(panel);
		color = c;
		mouseOverColor = m;
	}

	@Override
	public void draw(MatrixStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
		((mouseOverColor != null && isMouseOver()) ? mouseOverColor : color).draw(x, y, w, h);
	}
}