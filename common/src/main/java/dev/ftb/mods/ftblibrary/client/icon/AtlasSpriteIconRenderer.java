package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.AtlasSpriteIcon;
import dev.ftb.mods.ftblibrary.client.util.AtlasSpriteLookup;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public enum AtlasSpriteIconRenderer implements IconRenderer<AtlasSpriteIcon> {
    INSTANCE;

    @Override
    public void render(AtlasSpriteIcon icon, GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, getSprite(icon), x, y, w, h, icon.getColor().rgba());
    }

    @Override
    public int getPixelBufferFrameCount(AtlasSpriteIcon icon) {
        return getSprite(icon).contents().getFrameCount();
    }

    @Override
    public double aspectRatio(AtlasSpriteIcon icon) {
        var s = getSprite(icon);
        return (double) s.contents().width() / s.contents().height();
    }

    @Override
    public boolean isAspectRatioKnown() {
        return true;
    }

    private TextureAtlasSprite getSprite(AtlasSpriteIcon icon) {
        return AtlasSpriteLookup.find(icon.getSpriteId());
    }
}
