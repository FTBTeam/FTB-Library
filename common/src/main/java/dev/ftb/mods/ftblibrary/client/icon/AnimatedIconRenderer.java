package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.AnimatedIcon;
import net.minecraft.client.gui.GuiGraphics;

public enum AnimatedIconRenderer implements IconRenderer<AnimatedIcon> {
    INSTANCE;

    @Override
    public void render(AnimatedIcon icon, GuiGraphics graphics, int x, int y, int w, int h) {
        if (!icon.getList().isEmpty()) {
            Icon<?> subIcon = icon.getList().get((int) ((System.currentTimeMillis() / 1000L) % icon.getList().size()));
            IconHelper.renderIcon(subIcon, graphics, x, y, w, h);
        }
    }

    @Override
    public void renderStatic(AnimatedIcon icon, GuiGraphics graphics, int x, int y, int w, int h) {
        if (!icon.getList().isEmpty()) {
            Icon<?> subIcon = icon.getList().get((int) ((System.currentTimeMillis() / 1000L) % icon.getList().size()));
            IconHelper.renderIconStatic(subIcon, graphics, x, y, w, h);
        }
    }
}
