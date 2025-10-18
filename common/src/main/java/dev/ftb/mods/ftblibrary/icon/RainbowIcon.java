package dev.ftb.mods.ftblibrary.icon;

import dev.ftb.mods.ftblibrary.util.text.RainbowTextColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class RainbowIcon extends Icon {
    public static RainbowIcon RAINBOW = new RainbowIcon();

    @Override
    public void draw(GuiGraphics graphics, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        Integer[] colors = RainbowTextColor.getRainbowColors().get();

        int ticks = (int) Minecraft.getInstance().clientTickCount;
        int color =  colors[ticks % colors.length];

        graphics.fill(x, y, x + width, y + height, color);
    }
}
