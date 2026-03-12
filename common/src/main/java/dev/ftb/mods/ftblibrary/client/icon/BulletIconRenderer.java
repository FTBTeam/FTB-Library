package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.BulletIcon;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.MutableColor4I;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

import java.util.List;

public enum BulletIconRenderer implements IconRenderer<BulletIcon> {
    INSTANCE;

    private static final MutableColor4I DEFAULT_COLOR = Color4I.rgb(0xEDEDED).mutable();
    private static final MutableColor4I DEFAULT_BRIGHT_COLOR = Color4I.rgb(0xFFFFFF).mutable();
    private static final MutableColor4I DEFAULT_DARK_COLOR = Color4I.rgb(0xDDDDDD).mutable();

    @Override
    public void render(BulletIcon icon, GuiGraphics graphics, int x, int y, int w, int h) {
        int col, bright, dark;

        if (icon.getColor().isEmpty()) {
            col = DEFAULT_COLOR.rgba();
            bright = DEFAULT_BRIGHT_COLOR.rgba();
            dark = DEFAULT_DARK_COLOR.rgba();
        } else {
            col = icon.getColor().rgba();
            bright = icon.getBrightColor().rgba();
            dark = icon.getDarkColor().rgba();
        }

        boolean inverse = icon.isInverse();
        graphics.guiRenderState.submitGuiElement(new ArbitraryVertexRenderState(
                RenderPipelines.GUI,
                TextureSetup.noTexture(),
                graphics.pose(),
                x, y, w, h,
                List.of(
                        new Vertex(x, y + 1, 1, h - 2, inverse ? dark : bright),
                        new Vertex(x + w - 1, y + 1, 1, h - 2, inverse ? bright : dark),
                        new Vertex(x + 1, y, w - 2, 1, inverse ? dark : bright),
                        new Vertex(x + 1, y + h - 1, w - 2, 1, inverse ? bright : dark),
                        new Vertex(x + 1, y + 1, w - 2, h - 2, col)
                ),
                graphics.scissorStack.peek()
        ));
    }

    public record ArbitraryVertexRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            List<Vertex> vertices,
            int x,
            int y,
            int width,
            int height,
            @Nullable ScreenRectangle scissorArea,
            @Nullable ScreenRectangle bounds
    ) implements GuiElementRenderState {
        public ArbitraryVertexRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, int x, int y, int width, int height, List<Vertex> vertices, @Nullable ScreenRectangle screenRectangle) {
            this(pipeline, textureSetup, pose, vertices, x, y, width, height, screenRectangle, getBounds(x, y, width, height, pose, screenRectangle));
        }

        @Override
        public void buildVertices(VertexConsumer vertexConsumer) {
            for (Vertex vertex : vertices) {
                vertexConsumer.addVertexWith2DPose(pose, vertex.x(), vertex.y())
                        .setUv(vertex.u(), vertex.v())
                        .setColor(vertex.color);
            }
        }

        @Nullable
        private static ScreenRectangle getBounds(int x, int y, int width, int height, Matrix3x2f pose, @Nullable ScreenRectangle screenRectangle) {
            ScreenRectangle screenRectangle2 = (new ScreenRectangle(x, y, width - x, height - y)).transformMaxBounds(pose);
            return screenRectangle != null ? screenRectangle.intersection(screenRectangle2) : screenRectangle2;
        }
    }

    public record Vertex(
            float x,
            float y,
            float u,
            float v,
            int color
    ) {}
}
