package dev.ftb.mods.ftblibrary.icon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.util.List;


public class BulletIcon extends Icon {
    private static final MutableColor4I DEFAULT_COLOR = Color4I.rgb(0xEDEDED).mutable();
    private static final MutableColor4I DEFAULT_COLOR_B = Color4I.rgb(0xFFFFFF).mutable();
    private static final MutableColor4I DEFAULT_COLOR_D = Color4I.rgb(0xDDDDDD).mutable();

    private Color4I color, colorB, colorD;
    private boolean inverse;

    public BulletIcon() {
        color = Icon.empty();
        colorB = Icon.empty();
        colorD = Icon.empty();
        inverse = false;
    }

    @Override
    public BulletIcon copy() {
        var icon = new BulletIcon();
        icon.color = color;
        icon.colorB = colorB;
        icon.colorD = colorD;
        icon.inverse = inverse;
        return icon;
    }

    public BulletIcon setColor(Color4I col) {
        color = col;

        if (color.isEmpty()) {
            return this;
        }

        var c = color.mutable();
        c.addBrightness(18);
        colorB = c.copy();
        c = color.mutable();
        c.addBrightness(-18);
        colorD = c.copy();
        return this;
    }

    @Override
    public BulletIcon withColor(Color4I col) {
        return copy().setColor(col);
    }

    @Override
    public BulletIcon withTint(Color4I c) {
        return withColor(color.withTint(c));
    }

    public BulletIcon setInverse(boolean v) {
        inverse = v;
        return this;
    }

    @Override
    protected void setProperties(IconProperties properties) {
        super.setProperties(properties);
        inverse = properties.getBoolean("inverse", inverse);
    }

    @Override
    public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
        int c, cb, cd;

        if (color.isEmpty()) {
            c = DEFAULT_COLOR.rgba();
            cb = DEFAULT_COLOR_B.rgba();
            cd = DEFAULT_COLOR_D.rgba();
        } else {
            c = color.rgba();
            cb = colorB.rgba();
            cd = colorD.rgba();
        }

        graphics.guiRenderState.submitGuiElement(new ArbitraryVertexRenderState(
                RenderPipelines.GUI,
                TextureSetup.noTexture(),
                graphics.pose(),
                x, y, w, h,
                List.of(
                        new Vertex(x, y + 1, 1, h - 2, inverse ? cd : cb),
                        new Vertex(x + w - 1, y + 1, 1, h - 2, inverse ? cb : cd),
                        new Vertex(x + 1, y, w - 2, 1, inverse ? cd : cb),
                        new Vertex(x + 1, y + h - 1, w - 2, 1, inverse ? cb : cd),
                        new Vertex(x + 1, y + 1, w - 2, h - 2, c)
                ),
                graphics.scissorStack.peek()
        ));

//        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
//        VertexConsumer buffer = bufferSource.getBuffer(RenderType.gui());
//
//        GuiHelper.addRectToBuffer(graphics, buffer, x, y + 1, 1, h - 2, inverse ? cd : cb);
//        GuiHelper.addRectToBuffer(graphics, buffer, x + w - 1, y + 1, 1, h - 2, inverse ? cb : cd);
//        GuiHelper.addRectToBuffer(graphics, buffer, x + 1, y, w - 2, 1, inverse ? cd : cb);
//        GuiHelper.addRectToBuffer(graphics, buffer, x + 1, y + h - 1, w - 2, 1, inverse ? cb : cd);
//        GuiHelper.addRectToBuffer(graphics, buffer, x + 1, y + 1, w - 2, h - 2, c);
    }

    @Override
    public JsonElement getJson() {
        var o = new JsonObject();
        o.addProperty("id", "bullet");

        if (!color.isEmpty()) {
            o.add("color", color.getJson());
        }

        return o;
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
