package dev.ftb.mods.ftblibrary.ui.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftblibrary.ui.Theme;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;


public class LoadingScreen extends BaseScreen {
    private boolean startedLoading = false;
    private boolean isLoading = true;
    private Component[] title;
    private float timer;

    public LoadingScreen() {
        setSize(128, 128);
        title = new Component[0];
    }

    public LoadingScreen(Component t) {
        setSize(128, 128);
        title = new Component[]{t};
    }

    @Override
    public void addWidgets() {
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        if (!startedLoading) {
            startLoading();
            startedLoading = true;
        }

        if (isLoading()) {
            GuiHelper.drawHollowRect(graphics, x + width / 2 - 48, y + height / 2 - 8, 96, 16, Color4I.WHITE, true);

            var x1 = x + width / 2 - 48;
            var y1 = y + height / 2 - 8;
            var w1 = 96;
            var h1 = 16;

            var col = Color4I.WHITE;
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            GuiHelper.addRectToBuffer(graphics, buffer, x1, y1 + 1, 1, h1 - 2, col);
            GuiHelper.addRectToBuffer(graphics, buffer, x1 + w1 - 1, y1 + 1, 1, h1 - 2, col);
            GuiHelper.addRectToBuffer(graphics, buffer, x1 + 1, y1, w1 - 2, 1, col);
            GuiHelper.addRectToBuffer(graphics, buffer, x1 + 1, y1 + h1 - 1, w1 - 2, 1, col);

            x1 += 1;
            y1 += 1;
            w1 -= 2;
            h1 -= 2;

            DeltaTracker d = Minecraft.getInstance().getTimer();

            timer += d.getRealtimeDeltaTicks();
            timer = timer % (h1 * 2F);

            for (var oy = 0; oy < h1; oy++) {
                for (var ox = 0; ox < w1; ox++) {
                    var index = ox + oy + (int) timer;

                    if (index % (h1 * 2) < h1) {
                        col = Color4I.WHITE.withAlpha(200 - (index % h1) * 9);

                        GuiHelper.addRectToBuffer(graphics, buffer, x1 + ox, y1 + oy, 1, 1, col);
                    }
                }
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());

            var s = getText();

            if (s.length > 0) {
                for (var i = 0; i < s.length; i++) {
                    theme.drawString(graphics, s[i], x + width / 2, y - 26 + i * 12, Theme.CENTERED);
                }
            }
        } else {
            closeGui();
            finishLoading();
        }
    }

    public synchronized Component[] getText() {
        return title;
    }

    public synchronized void setText(Component... s) {
        title = s;
    }

    public synchronized void setFinished() {
        isLoading = false;
    }

    public void startLoading() {
    }

    public synchronized boolean isLoading() {
        return isLoading;
    }

    public void finishLoading() {
    }
}
