package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.AtlasSpriteIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.data.AtlasIds;

public enum AtlasSpriteIconRenderer implements IconRenderer<AtlasSpriteIcon> {
    INSTANCE;

    @Override
    public void render(AtlasSpriteIcon icon, GuiGraphics graphics, int x, int y, int w, int h) {
        // TODO: @since 21.11: This isn't ideal. We should really be looking this up or something?
        var isItem = icon.getSpriteId().getPath().startsWith("item/");
        var sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(isItem ? AtlasIds.ITEMS : AtlasIds.BLOCKS).getSprite(icon.getSpriteId());
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, w, h, icon.getColor().rgba());
    }
}
