package dev.ftb.mods.ftblibrary.icon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class IconAnimation extends Icon {
	public static Icon fromList(List<Icon> icons, boolean includeEmpty) {
		List<Icon> list = new ArrayList<>(icons.size());

		for (var icon : icons) {
			if (icon instanceof IconAnimation) {
				for (var icon1 : ((IconAnimation) icon).list) {
					if (includeEmpty || !icon1.isEmpty()) {
						list.add(icon1);
					}
				}
			} else if (includeEmpty || !icon.isEmpty()) {
				list.add(icon);
			}
		}

		if (list.isEmpty()) {
			return empty();
		} else if (list.size() == 1) {
			return list.get(0);
		}

		return new IconAnimation(list);
	}

	public final List<Icon> list;

	private IconAnimation(List<Icon> l) {
		list = l;
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
		if (!list.isEmpty()) {
			list.get((int) ((System.currentTimeMillis() / 1000L) % list.size())).draw(graphics, x, y, w, h);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void drawStatic(GuiGraphics graphics, int x, int y, int w, int h) {
		if (!list.isEmpty()) {
			list.get(0).drawStatic(graphics, x, y, w, h);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void draw3D(GuiGraphics graphics) {
		if (!list.isEmpty()) {
			list.get((int) ((System.currentTimeMillis() / 1000L) % list.size())).draw3D(graphics);
		}
	}

	@Override
	public JsonElement getJson() {
		var json = new JsonObject();
		json.addProperty("id", "animation");

		var array = new JsonArray();

		for (var icon : list) {
			array.add(icon.getJson());
		}

		json.add("icons", array);
		return json;
	}

	public int hashCode() {
		return list.hashCode();
	}

	public boolean equals(Object o) {
		return o == this || o instanceof IconAnimation && list.equals(((IconAnimation) o).list);
	}

	@Override
	@Nullable
	public Object getIngredient() {
		if (!list.isEmpty()) {
			return list.get((int) ((System.currentTimeMillis() / 1000L) % list.size())).getIngredient();
		}

		return null;
	}
}
