package dev.ftb.mods.ftblibrary.client.gui.widget;

import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;


public class ColorWidget extends Widget {
    public final Color4I color;
    public Color4I mouseOverColor;

    public ColorWidget(Panel panel, Color4I c, @Nullable Color4I m) {
        super(panel);
        color = c;
        mouseOverColor = m;
    }

    @Override
    public void draw(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h) {
        IconHelper.renderIcon(((mouseOverColor != null && isMouseOver()) ? mouseOverColor : color), graphics, x, y, w, h);
    }
}
