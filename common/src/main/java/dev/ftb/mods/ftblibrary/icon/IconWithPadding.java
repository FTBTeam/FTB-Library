package dev.ftb.mods.ftblibrary.icon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;

/**
 * @author LatvianModder
 */
public class IconWithPadding extends IconWithParent {
	public int padding;

	IconWithPadding(Icon p, int b) {
		super(p);
		padding = b;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
		x += padding;
		y += padding;
		w -= padding * 2;
		h -= padding * 2;
		parent.draw(graphics, x, y, w, h);
	}

	@Override
	public JsonElement getJson() {
		if (padding == 0) {
			return parent.getJson();
		}

		var json = new JsonObject();
		json.addProperty("id", "padding");
		json.addProperty("padding", padding);
		json.add("parent", parent.getJson());
		return json;
	}

	@Override
	public IconWithPadding copy() {
		return new IconWithPadding(parent.copy(), padding);
	}

	@Override
	public IconWithPadding withTint(Color4I color) {
		return new IconWithPadding(parent.withTint(color), padding);
	}

	@Override
	public IconWithPadding withColor(Color4I color) {
		return new IconWithPadding(parent.withColor(color), padding);
	}
}