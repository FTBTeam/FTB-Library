package dev.ftb.mods.ftblibrary.icon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;

/**
 * @author LatvianModder
 */
public class BulletIcon extends Icon {
	private static final MutableColor4I DEFAULT_COLOR = Color4I.rgb(0xEDEDED).mutable();
	private static final MutableColor4I DEFAULT_COLOR_B = Color4I.rgb(0xFFFFFF).mutable();
	private static final MutableColor4I DEFAULT_COLOR_D = Color4I.rgb(0xDDDDDD).mutable();

	private Color4I color, colorB, colorD;
	private boolean inverse;

	public BulletIcon() {
		color = Icon.empty();
		colorB = Icon.empty();
		colorD = Icon.empty();
		inverse = false;
	}

	@Override
	public BulletIcon copy() {
		var icon = new BulletIcon();
		icon.color = color;
		icon.colorB = colorB;
		icon.colorD = colorD;
		icon.inverse = inverse;
		return icon;
	}

	public BulletIcon setColor(Color4I col) {
		color = col;

		if (color.isEmpty()) {
			return this;
		}

		var c = color.mutable();
		c.addBrightness(18);
		colorB = c.copy();
		c = color.mutable();
		c.addBrightness(-18);
		colorD = c.copy();
		return this;
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
	@Environment(EnvType.CLIENT)
	public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
		Color4I c, cb, cd;

		if (color.isEmpty()) {
			c = DEFAULT_COLOR;
			cb = DEFAULT_COLOR_B;
			cd = DEFAULT_COLOR_D;
		} else {
			c = color;
			cb = colorB;
			cd = colorD;
		}

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		var tesselator = Tesselator.getInstance();
		var buffer = tesselator.getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		GuiHelper.addRectToBuffer(graphics, buffer, x, y + 1, 1, h - 2, inverse ? cd : cb);
		GuiHelper.addRectToBuffer(graphics, buffer, x + w - 1, y + 1, 1, h - 2, inverse ? cb : cd);
		GuiHelper.addRectToBuffer(graphics, buffer, x + 1, y, w - 2, 1, inverse ? cd : cb);
		GuiHelper.addRectToBuffer(graphics, buffer, x + 1, y + h - 1, w - 2, 1, inverse ? cb : cd);
		GuiHelper.addRectToBuffer(graphics, buffer, x + 1, y + 1, w - 2, h - 2, c);

		tesselator.end();
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