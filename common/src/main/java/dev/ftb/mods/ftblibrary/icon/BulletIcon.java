package dev.ftb.mods.ftblibrary.icon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.client.icon.BulletIconRenderer;
import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import net.minecraft.resources.Identifier;


public class BulletIcon extends Icon<BulletIcon> {
    public static final Identifier TYPE = FTBLibrary.rl("bullet");

    private Color4I color, brightColor, darkColor;
    private boolean inverse;

    public BulletIcon() {
        color = Icon.empty();
        brightColor = Icon.empty();
        darkColor = Icon.empty();
        inverse = false;
    }

    @Override
    public BulletIcon copy() {
        var icon = new BulletIcon();
        icon.color = color;
        icon.brightColor = brightColor;
        icon.darkColor = darkColor;
        icon.inverse = inverse;
        return icon;
    }

    public Color4I getColor() {
        return color;
    }

    public BulletIcon setColor(Color4I col) {
        color = col;

        if (color.isEmpty()) {
            return this;
        }

        var c = color.mutable();
        c.addBrightness(18);
        brightColor = c.copy();
        c = color.mutable();
        c.addBrightness(-18);
        darkColor = c.copy();
        return this;
    }

    public Color4I getBrightColor() {
        return brightColor;
    }

    public Color4I getDarkColor() {
        return darkColor;
    }

    public boolean isInverse() {
        return inverse;
    }

    @Override
    public BulletIcon withColor(Color4I col) {
        return copy().setColor(col);
    }

    @Override
    public BulletIcon withTint(Color4I c) {
        return withColor(color.withTint(c));
    }

    public BulletIcon setInverse(boolean v) {
        inverse = v;
        return this;
    }

    @Override
    protected void setProperties(IconProperties properties) {
        super.setProperties(properties);
        inverse = properties.getBoolean("inverse", inverse);
    }

    @Override
    public IconRenderer<BulletIcon> getRenderer() {
        return BulletIconRenderer.INSTANCE;
    }

    @Override
    public JsonElement getJson() {
        var o = new JsonObject();
        o.addProperty("id", "bullet");

        if (!color.isEmpty()) {
            o.add("color", color.getJson());
        }

        return o;
    }
}
