package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.PartIcon;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import static dev.ftb.mods.ftblibrary.client.icon.IconHelper.renderIcon;

public enum PartIconRenderer implements IconRenderer<PartIcon> {
    INSTANCE;

    @Override
    public void render(PartIcon icon, GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        if (w == icon.subWidth && h == icon.subHeight) {
            renderIcon(icon.all, graphics, x, y, w, h);
            return;
        }

        var c = icon.corner;
        var mw = w - c * 2;
        var mh = h - c * 2;

        renderIcon(icon.middleU, graphics, x + c, y, mw, c);
        renderIcon(icon.middleR, graphics, x + w - c, y + c, c, mh);
        renderIcon(icon.middleD, graphics, x + c, y + h - c, mw, c);
        renderIcon(icon.middleL, graphics, x, y + c, c, mh);

        renderIcon(icon.cornerNN, graphics, x, y, c, c);
        renderIcon(icon.cornerNP, graphics, x, y + h - c, c, c);
        renderIcon(icon.cornerPN, graphics, x + w - c, y, c, c);
        renderIcon(icon.cornerPP, graphics, x + w - c, y + h - c, c, c);

        renderIcon(icon.center, graphics, x + c, y + c, mw, mh);
    }
}
