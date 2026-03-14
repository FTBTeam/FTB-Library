package dev.ftb.mods.ftblibrary.icon;

import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import dev.ftb.mods.ftblibrary.client.icon.TextureAtlasSpriteIconRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class TextureAtlasSpriteIcon extends Icon<TextureAtlasSpriteIcon> {
    private final TextureAtlasSprite sprite;
    private final Color4I color;

    public TextureAtlasSpriteIcon(TextureAtlasSprite sprite) {
        this(sprite, Color4I.WHITE);
    }

    public TextureAtlasSpriteIcon(TextureAtlasSprite sprite, Color4I color) {
        this.sprite = sprite;
        this.color = color;
    }

    public TextureAtlasSprite getSprite() {
        return sprite;
    }

    public Color4I getColor() {
        return color;
    }

    @Override
    public TextureAtlasSpriteIcon copy() {
        return new TextureAtlasSpriteIcon(sprite);
    }

    @Override
    public TextureAtlasSpriteIcon withColor(Color4I color) {
        return new TextureAtlasSpriteIcon(sprite, color);
    }

    @Override
    public TextureAtlasSpriteIcon withTint(Color4I c) {
        return withColor(color.withTint(c));
    }


    @Override
    public IconRenderer<TextureAtlasSpriteIcon> getRenderer() {
        return TextureAtlasSpriteIconRenderer.INSTANCE;
    }
}
