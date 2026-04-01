package dev.ftb.mods.ftblibrary.icon;

import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import dev.ftb.mods.ftblibrary.client.icon.TextureAtlasSpriteIconRenderer;
import dev.ftb.mods.ftblibrary.client.util.TextureAtlasSpriteRef;

public class TextureAtlasSpriteIcon extends Icon<TextureAtlasSpriteIcon> {
    private final TextureAtlasSpriteRef spriteRef;
    private final Color4I color;

    public TextureAtlasSpriteIcon(TextureAtlasSpriteRef spriteRef) {
        this(spriteRef, Color4I.WHITE);
    }

    public TextureAtlasSpriteIcon(TextureAtlasSpriteRef spriteRef, Color4I color) {
        this.spriteRef = spriteRef;
        this.color = color;
    }

    public TextureAtlasSpriteRef getSpriteRef() {
        return spriteRef;
    }

    public Color4I getColor() {
        return color;
    }

    @Override
    public TextureAtlasSpriteIcon copy() {
        return new TextureAtlasSpriteIcon(spriteRef);
    }

    @Override
    public TextureAtlasSpriteIcon withColor(Color4I color) {
        return new TextureAtlasSpriteIcon(spriteRef, color);
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
