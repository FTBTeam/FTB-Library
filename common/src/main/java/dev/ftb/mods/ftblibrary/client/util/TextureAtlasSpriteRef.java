package dev.ftb.mods.ftblibrary.client.util;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.TextureAtlasSpriteIcon;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public record TextureAtlasSpriteRef(TextureAtlasSprite sprite) {
    public TextureAtlasSpriteIcon createIcon(Color4I color) {
        return new TextureAtlasSpriteIcon(this, color);
    }

    public TextureAtlasSpriteIcon createIcon() {
        return new TextureAtlasSpriteIcon(this, Color4I.WHITE);
    }
}
