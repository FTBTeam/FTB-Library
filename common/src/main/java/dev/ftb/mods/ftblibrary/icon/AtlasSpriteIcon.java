package dev.ftb.mods.ftblibrary.icon;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.client.icon.AtlasSpriteIconRenderer;
import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import net.minecraft.resources.Identifier;

public class AtlasSpriteIcon extends Icon<AtlasSpriteIcon> implements IResourceIcon {
    public static final Identifier TYPE = FTBLibrary.rl("atlas_sprite");

    private final Identifier spriteId;
    private final Color4I color;

    AtlasSpriteIcon(Identifier spriteId) {
        this(spriteId, Color4I.WHITE);
    }

    AtlasSpriteIcon(Identifier spriteId, Color4I color) {
        this.spriteId = spriteId;
        this.color = color;
    }

    public Identifier getSpriteId() {
        return spriteId;
    }

    public Color4I getColor() {
        return color;
    }

    @Override
    public String toString() {
        return spriteId.toString();
    }

    @Override
    public AtlasSpriteIcon copy() {
        return new AtlasSpriteIcon(spriteId);
    }

    @Override
    public AtlasSpriteIcon withColor(Color4I color) {
        return new AtlasSpriteIcon(spriteId, color);
    }

    @Override
    public AtlasSpriteIcon withTint(Color4I c) {
        return withColor(color.withTint(c));
    }

    @Override
    public IconRenderer<AtlasSpriteIcon> getRenderer() {
        return AtlasSpriteIconRenderer.INSTANCE;
    }

    @Override
    public Identifier getResourceId() {
        return getSpriteId();
    }
}
