package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.TextureAtlasSpriteIcon;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public enum TextureAtlasSpriteIconRenderer implements IconRenderer<TextureAtlasSpriteIcon> {
    INSTANCE;

    @Override
    public void render(TextureAtlasSpriteIcon icon, GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon.getSpriteRef().sprite(), x, y, w, h, icon.getColor().rgba());
    }
}
