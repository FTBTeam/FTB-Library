package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.CombinedIcon;
import net.minecraft.client.gui.GuiGraphics;

public enum CombinedIconRenderer implements IconRenderer<CombinedIcon> {
    INSTANCE;

    @Override
    public void render(CombinedIcon icon, GuiGraphics graphics, int x, int y, int w, int h) {
        for (var subIcon : icon.list) {
            IconHelper.renderIcon(subIcon, graphics, x, y, w, h);
        }
    }

    @Override
    public void renderStatic(CombinedIcon icon, GuiGraphics graphics, int x, int y, int w, int h) {
        for (var subIcon : icon.list) {
            IconHelper.renderIconStatic(subIcon, graphics, x, y, w, h);
        }
    }
}
