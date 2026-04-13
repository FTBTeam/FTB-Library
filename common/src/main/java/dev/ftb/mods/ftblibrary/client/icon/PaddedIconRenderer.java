package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.PaddedIcon;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public enum PaddedIconRenderer implements IconRenderer<PaddedIcon> {
    INSTANCE;

    @Override
    public void render(PaddedIcon icon, GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        x += icon.getPadding();
        y += icon.getPadding();
        w -= icon.getPadding() * 2;
        h -= icon.getPadding() * 2;

        IconHelper.renderIcon(icon.getParent(), graphics, x, y, w, h);
    }
}
