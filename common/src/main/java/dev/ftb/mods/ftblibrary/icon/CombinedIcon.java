package dev.ftb.mods.ftblibrary.icon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author LatvianModder
 */
public class CombinedIcon extends Icon {
	public static Icon getCombined(Collection<Icon> icons) {
		List<Icon> list = new ArrayList<>(icons.size());

		for (var icon : icons) {
			if (!icon.isEmpty()) {
				list.add(icon);
			}
		}

		if (list.isEmpty()) {
			return Color4I.EMPTY;
		} else if (list.size() == 1) {
			return list.get(0);
		}

		return new CombinedIcon(list);
	}

	public final List<Icon> list;

	CombinedIcon(Collection<Icon> icons) {
		list = new ArrayList<>(icons.size());

		for (var icon : icons) {
			if (!icon.isEmpty()) {
				list.add(icon);
			}
		}
	}

	CombinedIcon(Icon o1, Icon o2) {
		list = new ArrayList<>(2);
		list.add(o1);
		list.add(o2);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void draw(PoseStack matrixStack, int x, int y, int w, int h) {
		for (var icon : list) {
			icon.draw(matrixStack, x, y, w, h);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void drawStatic(PoseStack matrixStack, int x, int y, int w, int h) {
		for (var icon : list) {
			icon.drawStatic(matrixStack, x, y, w, h);
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void draw3D(PoseStack matrixStack) {
		for (var icon : list) {
			icon.draw3D(matrixStack);
		}
	}

	@Override
	public JsonElement getJson() {
		var json = new JsonArray();

		for (var o : list) {
			json.add(o.getJson());
		}

		return json;
	}

	public int hashCode() {
		return list.hashCode();
	}

	public boolean equals(Object o) {
		return o == this || o instanceof CombinedIcon && list.equals(((CombinedIcon) o).list);
	}
}
