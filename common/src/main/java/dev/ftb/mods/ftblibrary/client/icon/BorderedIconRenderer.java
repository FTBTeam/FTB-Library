package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.client.gui.GuiHelper;
import dev.ftb.mods.ftblibrary.icon.BorderedIcon;
import net.minecraft.client.gui.GuiGraphics;

public enum BorderedIconRenderer implements IconRenderer<BorderedIcon> {
    INSTANCE;

    @Override
    public void render(BorderedIcon icon, GuiGraphics graphics, int x, int y, int w, int h) {
        IconHelper.renderIcon(icon.getParent(), graphics, x + 1, y + 1, w - 2, h - 2);
        GuiHelper.drawHollowRect(graphics, x, y, w, h, icon.getColor(), icon.isRoundEdges());
    }
}
