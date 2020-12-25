package com.feed_the_beast.mods.ftbguilibrary.misc;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiHelper;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.opengl.GL11;

/**
 * @author LatvianModder
 */
public class GuiLoading extends GuiBase {
    private boolean startedLoading = false;
    private boolean isLoading = true;
    private Component[] title;
    public float timer;

    public GuiLoading() {
        setSize(128, 128);
        title = new Component[0];
    }

    public GuiLoading(Component t) {
        setSize(128, 128);
        title = new Component[]{t};
    }

    @Override
    public void addWidgets() {
    }

    @Override
    public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
        if (!startedLoading) {
            startLoading();
            startedLoading = true;
        }

        if (isLoading()) {
            GuiHelper.drawHollowRect(matrixStack, x + width / 2 - 48, y + height / 2 - 8, 96, 16, Color4I.WHITE, true);

            int x1 = x + width / 2 - 48;
            int y1 = y + height / 2 - 8;
            int w1 = 96;
            int h1 = 16;

            Color4I col = Color4I.WHITE;
            GlStateManager._disableTexture();
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);

            GuiHelper.addRectToBuffer(matrixStack, buffer, x1, y1 + 1, 1, h1 - 2, col);
            GuiHelper.addRectToBuffer(matrixStack, buffer, x1 + w1 - 1, y1 + 1, 1, h1 - 2, col);
            GuiHelper.addRectToBuffer(matrixStack, buffer, x1 + 1, y1, w1 - 2, 1, col);
            GuiHelper.addRectToBuffer(matrixStack, buffer, x1 + 1, y1 + h1 - 1, w1 - 2, 1, col);

            x1 += 1;
            y1 += 1;
            w1 -= 2;
            h1 -= 2;

            timer += Minecraft.getInstance().getDeltaFrameTime();
            timer = timer % (h1 * 2F);

            for (int oy = 0; oy < h1; oy++) {
                for (int ox = 0; ox < w1; ox++) {
                    int index = ox + oy + (int) timer;

                    if (index % (h1 * 2) < h1) {
                        col = Color4I.WHITE.withAlpha(200 - (index % h1) * 9);

                        GuiHelper.addRectToBuffer(matrixStack, buffer, x1 + ox, y1 + oy, 1, 1, col);
                    }
                }
            }

            tessellator.end();
            GlStateManager._enableTexture();

            Component[] s = getText();

            if (s.length > 0) {
                for (int i = 0; i < s.length; i++) {
                    theme.drawString(matrixStack, s[i], x + width / 2, y - 26 + i * 12, Theme.CENTERED);
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