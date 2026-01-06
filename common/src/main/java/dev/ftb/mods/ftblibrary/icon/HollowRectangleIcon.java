package dev.ftb.mods.ftblibrary.icon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ftb.mods.ftblibrary.client.icon.HollowRectangleIconRenderer;
import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;

public class HollowRectangleIcon extends Icon<HollowRectangleIcon> {
    private final Color4I color;
    private boolean roundEdges;

    public HollowRectangleIcon(Color4I color, boolean roundEdges) {
        this.color = color;
        this.roundEdges = roundEdges;
    }

    public Color4I getColor() {
        return color;
    }

    public boolean isRoundEdges() {
        return roundEdges;
    }

    @Override
    public HollowRectangleIcon copy() {
        return new HollowRectangleIcon(color, roundEdges);
    }

    @Override
    public HollowRectangleIcon withColor(Color4I color) {
        return new HollowRectangleIcon(color, roundEdges);
    }

    @Override
    public HollowRectangleIcon withTint(Color4I c) {
        return withColor(color.withTint(c));
    }

    @Override
    protected void setProperties(IconProperties properties) {
        super.setProperties(properties);

        roundEdges = properties.getBoolean("round_edges", roundEdges);
    }

    @Override
    public IconRenderer<HollowRectangleIcon> getRenderer() {
        return HollowRectangleIconRenderer.INSTANCE;
    }

    @Override
    public JsonElement getJson() {
        var json = new JsonObject();

        json.addProperty("id", "hollow_rectangle");
        json.add("color", color.getJson());
        if (roundEdges) {
            json.addProperty("round_edges", true);
        }

        return json;
    }
}
