package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.CombinedIcon;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public enum CombinedIconRenderer implements IconRenderer<CombinedIcon> {
    INSTANCE;

    @Override
    public void render(CombinedIcon icon, GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        for (var subIcon : icon.list) {
            IconHelper.renderIcon(subIcon, graphics, x, y, w, h);
        }
    }

    @Override
    public void renderStatic(CombinedIcon icon, GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        for (var subIcon : icon.list) {
            IconHelper.renderIconStatic(subIcon, graphics, x, y, w, h);
        }
    }
}
