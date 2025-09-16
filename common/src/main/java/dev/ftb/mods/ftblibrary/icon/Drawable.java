package dev.ftb.mods.ftblibrary.icon;

import net.minecraft.client.gui.GuiGraphics;


public interface Drawable {
    void draw(GuiGraphics graphics, int x, int y, int w, int h);

    default void drawStatic(GuiGraphics graphics, int x, int y, int w, int h) {
        draw(graphics, x, y, w, h);
    }

    default void draw3D(GuiGraphics graphics) {
        graphics.pose().pushMatrix();
        graphics.pose().scale(1F / 16F, 1F / 16F);
        draw(graphics, -8, -8, 16, 16);
        graphics.pose().popMatrix();
    }
}
