package dev.ftb.mods.ftbguilibrary.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbguilibrary.icon.Color4I;
import org.jetbrains.annotations.Nullable;

/**
 * @author LatvianModder
 */
public class ColorWidget extends Widget {
	public final Color4I color;
	public Color4I mouseOverColor;

	public ColorWidget(Panel panel, Color4I c, @Nullable Color4I m) {
		super(panel);
		color = c;
		mouseOverColor = m;
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		((mouseOverColor != null && isMouseOver()) ? mouseOverColor : color).draw(matrixStack, x, y, w, h);
	}
}