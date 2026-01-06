package dev.ftb.mods.ftblibrary.icon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ftb.mods.ftblibrary.client.icon.BorderedIconRenderer;
import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import net.minecraft.client.gui.GuiGraphics;


public class BorderedIcon extends IconWithParent<BorderedIcon> {
    public static final Icon<?> BUTTON_GRAY = Color4I.rgb(0x212121).withBorder(Color4I.rgb(0x141414), false);
    public static final Icon<?> BUTTON_RED = Color4I.rgb(0x1581B6).withBorder(Color4I.rgb(0xBF3726), false);
    public static final Icon<?> BUTTON_GREEN = Color4I.rgb(0x98C600).withBorder(Color4I.rgb(0x438700), false);
    public static final Icon<?> BUTTON_BLUE = Color4I.rgb(0x80C7F2).withBorder(Color4I.rgb(0x1581B6), false);

    public static final Icon<?> BUTTON_ROUND_GRAY = Color4I.rgb(0x212121).withBorder(Color4I.rgb(0x141414), true);
    public static final Icon<?> BUTTON_ROUND_RED = Color4I.rgb(0x1581B6).withBorder(Color4I.rgb(0xBF3726), true);
    public static final Icon<?> BUTTON_ROUND_GREEN = Color4I.rgb(0x98C600).withBorder(Color4I.rgb(0x438700), true);
    public static final Icon<?> BUTTON_ROUND_BLUE = Color4I.rgb(0x80C7F2).withBorder(Color4I.rgb(0x1581B6), true);

    private final Color4I color;
    private final boolean roundEdges;

    BorderedIcon(Icon i, Color4I c, boolean r) {
        super(i);
        color = c;
        roundEdges = r;
    }

    public Color4I getColor() {
        return color;
    }

    public boolean isRoundEdges() {
        return roundEdges;
    }

    @Override
    public JsonElement getJson() {
        var o = new JsonObject();
        o.addProperty("id", "border");
        o.add("icon", getParent().getJson());
        o.add("color", color.getJson());

        if (roundEdges) {
            o.addProperty("round_edges", true);
        }

        return o;
    }

    @Override
    public BorderedIcon copy() {
        return new BorderedIcon(getParent().copy(), color.copy(), roundEdges);
    }

    @Override
    public BorderedIcon withTint(Color4I tint) {
        return new BorderedIcon(getParent(), color.withTint(tint), roundEdges);
    }

    @Override
    public IconRenderer<BorderedIcon> getRenderer() {
        return BorderedIconRenderer.INSTANCE;
    }

    @Override
    public BorderedIcon withColor(Color4I color) {
        return new BorderedIcon(getParent(), color, roundEdges);
    }
}
