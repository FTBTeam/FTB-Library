package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.HollowRectangleIcon;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import net.minecraft.client.gui.GuiGraphics;

public enum HollowRectangleIconRenderer implements IconRenderer<HollowRectangleIcon> {
    INSTANCE;

    @Override
    public void render(HollowRectangleIcon icon, GuiGraphics graphics, int x, int y, int w, int h) {
        GuiHelper.drawHollowRect(graphics, x, y, w, h, icon.getColor(), icon.isRoundEdges());
    }
}
