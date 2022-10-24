package dev.ftb.mods.ftblibrary.ui.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ImageConfig;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.Theme;
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
    public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
    }

    @Override
    public void drawForeground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
        Component txt = Component.translatable("ftblibrary.select_image.scanning");
        int width = theme.getFont().width(txt);
        theme.getFont().draw(matrixStack, txt, (getScreen().getGuiScaledWidth() - width) / 2f, (getScreen().getGuiScaledHeight() - theme.getFontHeight()) / 2f, 0xFFFFFFFF);
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
