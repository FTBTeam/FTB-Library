package dev.ftb.mods.ftblibrary.icon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import dev.ftb.mods.ftblibrary.client.icon.PaddedIconRenderer;

public class PaddedIcon extends IconWithParent<PaddedIcon> {
    private final int padding;

    public PaddedIcon(Icon parent, int padding) {
        super(parent);

        this.padding = padding;
    }

    public int getPadding() {
        return padding;
    }

    @Override
    public JsonElement getJson() {
        if (padding == 0) {
            return getParent().getJson();
        }

        var json = new JsonObject();
        json.addProperty("id", "padding");
        json.addProperty("padding", padding);
        json.add("parent", getParent().getJson());
        return json;
    }

    @Override
    public PaddedIcon copy() {
        return new PaddedIcon(getParent().copy(), padding);
    }

    @Override
    public PaddedIcon withTint(Color4I color) {
        return new PaddedIcon(getParent().withTint(color), padding);
    }

    @Override
    public IconRenderer<PaddedIcon> getRenderer() {
        return PaddedIconRenderer.INSTANCE;
    }

    @Override
    public PaddedIcon withColor(Color4I color) {
        return new PaddedIcon(getParent().withColor(color), padding);
    }
}
