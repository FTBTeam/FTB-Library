package com.feed_the_beast.mods.ftbguilibrary.icon;

import com.feed_the_beast.mods.ftbguilibrary.widget.GuiHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

/**
 * @author LatvianModder
 */
public class BulletIcon extends Icon
{
	private static final MutableColor4I DEFAULT_COLOR = Color4I.rgb(0xEDEDED).mutable();
	private static final MutableColor4I DEFAULT_COLOR_B = Color4I.rgb(0xFFFFFF).mutable();
	private static final MutableColor4I DEFAULT_COLOR_D = Color4I.rgb(0xDDDDDD).mutable();

	private Color4I color, colorB, colorD;
	private boolean inverse;

	public BulletIcon()
	{
		color = Icon.EMPTY;
		colorB = Icon.EMPTY;
		colorD = Icon.EMPTY;
		inverse = false;
	}

	@Override
	public BulletIcon copy()
	{
		BulletIcon icon = new BulletIcon();
		icon.color = color;
		icon.colorB = colorB;
		icon.colorD = colorD;
		icon.inverse = inverse;
		return icon;
	}

	public BulletIcon setColor(Color4I col)
	{
		color = col;

		if (color.isEmpty())
		{
			return this;
		}

		MutableColor4I c = color.mutable();
		c.addBrightness(18);
		colorB = c.copy();
		c = color.mutable();
		c.addBrightness(-18);
		colorD = c.copy();
		return this;
	}

	@Override
	public BulletIcon withColor(Color4I col)
	{
		return copy().setColor(col);
	}

	@Override
	public BulletIcon withTint(Color4I c)
	{
		return withColor(color.withTint(c));
	}

	public BulletIcon setInverse(boolean v)
	{
		inverse = v;
		return this;
	}

	@Override
	protected void setProperties(IconProperties properties)
	{
		super.setProperties(properties);
		inverse = properties.getBoolean("inverse", inverse);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void draw(int x, int y, int w, int h)
	{
		Color4I c, cb, cd;

		if (color.isEmpty())
		{
			c = DEFAULT_COLOR;
			cb = DEFAULT_COLOR_B;
			cd = DEFAULT_COLOR_D;
		}
		else
		{
			c = color;
			cb = colorB;
			cd = colorD;
		}

		RenderSystem.disableTexture();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		GuiHelper.addRectToBuffer(buffer, x, y + 1, 1, h - 2, inverse ? cd : cb);
		GuiHelper.addRectToBuffer(buffer, x + w - 1, y + 1, 1, h - 2, inverse ? cb : cd);
		GuiHelper.addRectToBuffer(buffer, x + 1, y, w - 2, 1, inverse ? cd : cb);
		GuiHelper.addRectToBuffer(buffer, x + 1, y + h - 1, w - 2, 1, inverse ? cb : cd);
		GuiHelper.addRectToBuffer(buffer, x + 1, y + 1, w - 2, h - 2, c);

		tessellator.draw();
		RenderSystem.enableTexture();
		RenderSystem.color4f(1F, 1F, 1F, 1F);
	}

	@Override
	public JsonElement getJson()
	{
		JsonObject o = new JsonObject();
		o.addProperty("id", "bullet");

		if (!color.isEmpty())
		{
			o.add("color", color.getJson());
		}

		return o;
	}
}