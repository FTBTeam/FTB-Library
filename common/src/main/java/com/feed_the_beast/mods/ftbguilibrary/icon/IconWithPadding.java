package com.feed_the_beast.mods.ftbguilibrary.icon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * @author LatvianModder
 */
public class IconWithPadding extends IconWithParent
{
	public int padding;

	IconWithPadding(Icon p, int b)
	{
		super(p);
		padding = b;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void draw(PoseStack matrixStack, int x, int y, int w, int h)
	{
		x += padding;
		y += padding;
		w -= padding * 2;
		h -= padding * 2;
		parent.draw(matrixStack, x, y, w, h);
	}

	@Override
	public JsonElement getJson()
	{
		if (padding == 0)
		{
			return parent.getJson();
		}

		JsonObject json = new JsonObject();
		json.addProperty("id", "padding");
		json.addProperty("padding", padding);
		json.add("parent", parent.getJson());
		return json;
	}

	@Override
	public IconWithPadding copy()
	{
		return new IconWithPadding(parent.copy(), padding);
	}

	@Override
	public IconWithPadding withTint(Color4I color)
	{
		return new IconWithPadding(parent.withTint(color), padding);
	}

	@Override
	public IconWithPadding withColor(Color4I color)
	{
		return new IconWithPadding(parent.withColor(color), padding);
	}
}