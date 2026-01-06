package dev.ftb.mods.ftblibrary.icon;

import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import dev.ftb.mods.ftblibrary.client.icon.RainbowIconRenderer;

public class RainbowIcon extends Icon<RainbowIcon> {
    public static RainbowIcon RAINBOW = new RainbowIcon();

    @Override
    public IconRenderer<RainbowIcon> getRenderer() {
        return RainbowIconRenderer.INSTANCE;
    }
}
