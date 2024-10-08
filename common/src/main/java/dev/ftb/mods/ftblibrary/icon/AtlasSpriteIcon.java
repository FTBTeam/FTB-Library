package dev.ftb.mods.ftblibrary.icon;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
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
        var sprite = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(id);

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

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
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
        BufferUploader.drawWithShader(buffer.buildOrThrow());
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
