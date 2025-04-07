package dev.ftb.mods.ftblibrary.icon;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class AtlasSpriteIcon extends Icon implements IResourceIcon {
    private final ResourceLocation id;
    private final Color4I color;

    AtlasSpriteIcon(ResourceLocation id) {
        this(id, Color4I.WHITE);
    }

    AtlasSpriteIcon(ResourceLocation id, Color4I color) {
        this.id = id;
        this.color = color;
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
        var sprite = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(id);

        if (sprite == null) {
            return;
        }

        var m = graphics.pose().last().pose();

        var r = color.redi();
        var g = color.greeni();
        var b = color.bluei();
        var a = color.alphai();

        var minU = sprite.getU0();
        var minV = sprite.getV0();
        var maxU = sprite.getU1();
        var maxV = sprite.getV1();

        // TODO: Validate
        RenderType renderType = RenderType.guiTextured(id);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer buffer = bufferSource.getBuffer(renderType);

        buffer.addVertex(m, x, y, 0F)
                .setUv(minU, minV)
                .setColor(r, g, b, a);
        buffer.addVertex(m, x, y + h, 0F)
                .setUv(minU, maxV)
                .setColor(r, g, b, a);
        buffer.addVertex(m, x + w, y + h, 0F)
                .setUv(maxU, maxV)
                .setColor(r, g, b, a);
        buffer.addVertex(m, x + w, y, 0F)
                .setUv(maxU, minV)
                .setColor(r, g, b, a);
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean hasPixelBuffer() {
        return true;
    }

    @Override
    @Nullable
    public PixelBuffer createPixelBuffer() {
        try {
            return PixelBuffer.from(Minecraft.getInstance().getResourceManager().getResource(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "textures/" + id.getPath() + ".png")).orElseThrow().open());
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public AtlasSpriteIcon copy() {
        return new AtlasSpriteIcon(id);
    }

    @Override
    public AtlasSpriteIcon withColor(Color4I color) {
        return new AtlasSpriteIcon(id, color);
    }

    @Override
    public AtlasSpriteIcon withTint(Color4I c) {
        return withColor(color.withTint(c));
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return getId();
    }
}
