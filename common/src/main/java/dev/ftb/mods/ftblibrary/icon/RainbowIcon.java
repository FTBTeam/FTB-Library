package dev.ftb.mods.ftblibrary.icon;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftblibrary.util.text.RainbowTextColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;

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

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        GuiHelper.addRectToBuffer(graphics, buffer, x, y, width, height, Color4I.rgb(color));
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }
}
