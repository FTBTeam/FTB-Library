package dev.ftb.mods.ftblibrary.icon;

import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class AtlasSpriteIcon extends Icon implements IResourceIcon {
    private final Identifier id;
    private final Color4I color;

    AtlasSpriteIcon(Identifier id) {
        this(id, Color4I.WHITE);
    }

    AtlasSpriteIcon(Identifier id, Color4I color) {
        this.id = id;
        this.color = color;
    }

    public Identifier getId() {
        return id;
    }

    @Override
    public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
        var sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS).getSprite(id);

        if (sprite == null) {
            return;
        }

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, w, h, color.rgba());
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
            Identifier loc = Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/" + id.getPath() + ".png");
            return PixelBuffer.from(Minecraft.getInstance().getResourceManager().getResource(loc).orElseThrow().open());
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public int getPixelBufferFrameCount() {
        var sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS).getSprite(id);

        return sprite.contents().getFrameCount();
    }

    @Override
    public double aspectRatio() {
        var sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS).getSprite(id);

        return (double) sprite.contents().width() / (double) sprite.contents().height();
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
    public Identifier getIdentifier() {
        return getId();
    }
}
