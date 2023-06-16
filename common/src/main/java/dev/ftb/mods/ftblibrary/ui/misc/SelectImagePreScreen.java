package dev.ftb.mods.ftblibrary.ui.misc;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ImageConfig;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.Theme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SelectImagePreScreen extends BaseScreen {
    private final ImageConfig i;
    private final ConfigCallback c;
    private int counter = 3;

    public SelectImagePreScreen(ImageConfig i, ConfigCallback c) {
        this.i = i;
        this.c = c;
    }

    @Override
    public boolean onInit() {
        return setFullscreen();
    }

    @Override
    public void addWidgets() {
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
    }

    @Override
    public void drawForeground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        Component txt = Component.translatable("ftblibrary.select_image.scanning");
        int width = theme.getFont().width(txt);
        graphics.drawString(theme.getFont(), txt, (getScreen().getGuiScaledWidth() - width) / 2, (getScreen().getGuiScaledHeight() - theme.getFontHeight()) / 2, 0xFFFFFFFF);
    }

    @Override
    public void tick() {
        super.tick();

        if (--counter <= 0) {
            closeGui(true);
            new SelectImageScreen(i, c).openGuiLater();
        }
    }
}
