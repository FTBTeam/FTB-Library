package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.AtlasSpriteIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;

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

    private TextureAtlasSprite getSprite(AtlasSpriteIcon icon) {
        // TODO: @since 21.11: This isn't ideal. We should really be looking this up or something?
        var isItem = icon.getSpriteId().getPath().startsWith("item/");
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(isItem ? AtlasIds.ITEMS : AtlasIds.BLOCKS).getSprite(icon.getSpriteId());
    }
}
