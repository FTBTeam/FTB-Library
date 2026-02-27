package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.RainbowIcon;
import dev.ftb.mods.ftblibrary.util.text.RainbowTextColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public enum RainbowIconRenderer implements IconRenderer<RainbowIcon> {
    INSTANCE;

    @Override
    public void render(RainbowIcon icon, GuiGraphics graphics, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }

        Integer[] colors = RainbowTextColor.getRainbowColors().get();

        int ticks = (int) Minecraft.getInstance().clientTickCount;
        int color =  colors[ticks % colors.length];

        graphics.fill(x, y, x + w, y + h, color);
    }
}
