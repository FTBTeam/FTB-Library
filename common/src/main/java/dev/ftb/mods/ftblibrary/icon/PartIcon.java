package dev.ftb.mods.ftblibrary.icon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;


public class PartIcon extends IconWithParent {
	public final Icon parent;
	public int textureWidth, textureHeight;
	public int textureU, textureV, corner, subWidth, subHeight;

	private Icon all, middleU, middleD, middleL, middleR, cornerNN, cornerPN, cornerNP, cornerPP, center;

	public static PartIcon wholeTexture(String textureId, int textureWidth, int textureHeight, int corner) {
		return new PartIcon(textureId, 0, 0, textureWidth, textureHeight, corner, textureWidth, textureHeight);
	}

	public PartIcon(Icon icon, int textureU, int textureV, int subWidth, int subHeight, int corner, int textureWidth, int textureHeight) {super(icon);
		parent = icon;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.textureU = textureU;
		this.textureV = textureV;
		this.subWidth = subWidth;
		this.subHeight = subHeight;
		this.corner = corner;
		updateParts();
	}

	public PartIcon(Icon icon, int x, int y, int w, int h, int c) {
		this(icon, x, y, w, h, c, 256, 256);
	}

	public PartIcon(String iconId, int textureU, int textureV, int subWidth, int subHeight, int corner, int textureWidth, int textureHeight) {
		this(Icon.getIcon(iconId), textureU, textureV, subWidth, subHeight, corner, textureWidth, textureHeight);
	}

	public PartIcon(Icon icon) {
		this(icon, 0, 0, 256, 256, 6);
	}

	public PartIcon setTextureSize(int w, int h) {
		textureWidth = w;
		textureHeight = h;
		return this;
	}

	private Icon get(int x, int y, int w, int h) {
		return parent.withUV(textureU + x, textureV + y, w, h, textureWidth, textureHeight);
	}

	public void updateParts() {
		var mw = subWidth - corner * 2;
		var mh = subHeight - corner * 2;
		all = get(0, 0, subWidth, subHeight);
		middleU = get(corner, 0, mw, corner);
		middleD = get(corner, subHeight - corner, mw, corner);
		middleL = get(0, corner, corner, mh);
		middleR = get(subWidth - corner, corner, corner, mh);
		cornerNN = get(0, 0, corner, corner);
		cornerPN = get(subWidth - corner, 0, corner, corner);
		cornerNP = get(0, subHeight - corner, corner, corner);
		cornerPP = get(subWidth - corner, subHeight - corner, corner, corner);
		center = get(corner, corner, mw, mh);
	}

	@Override
	public PartIcon copy() {
		var icon = new PartIcon(parent.copy());
		icon.textureU = textureU;
		icon.textureV = textureV;
		icon.subWidth = subWidth;
		icon.subHeight = subHeight;
		icon.corner = corner;
		icon.textureWidth = textureWidth;
		icon.textureHeight = textureHeight;
		return icon;
	}

	@Override
	protected void setProperties(IconProperties properties) {
		super.setProperties(properties);
		textureU = properties.getInt("x", textureU);
		textureV = properties.getInt("y", textureV);
		subWidth = properties.getInt("width", subHeight);
		subHeight = properties.getInt("height", subHeight);
		corner = properties.getInt("corner", corner);
		textureWidth = properties.getInt("texture_w", textureWidth);
		textureHeight = properties.getInt("texture_h", textureHeight);

		var s = properties.getString("pos", "");

		if (!s.isEmpty()) {
			var s1 = s.split(",", 4);

			if (s1.length == 4) {
				textureU = Integer.parseInt(s1[0]);
				textureV = Integer.parseInt(s1[1]);
				subWidth = Integer.parseInt(s1[2]);
				subHeight = Integer.parseInt(s1[3]);
			}
		}

		updateParts();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
		if (w == subWidth && h == subHeight) {
			all.draw(graphics, x, y, w, h);
			return;
		}

		var c = corner;
		var mw = w - c * 2;
		var mh = h - c * 2;

		middleU.draw(graphics, x + c, y, mw, c);
		middleR.draw(graphics, x + w - c, y + c, c, mh);
		middleD.draw(graphics, x + c, y + h - c, mw, c);
		middleL.draw(graphics, x, y + c, c, mh);

		cornerNN.draw(graphics, x, y, c, c);
		cornerNP.draw(graphics, x, y + h - c, c, c);
		cornerPN.draw(graphics, x + w - c, y, c, c);
		cornerPP.draw(graphics, x + w - c, y + h - c, c, c);

		center.draw(graphics, x + c, y + c, mw, mh);
	}

	@Override
	public JsonElement getJson() {
		var json = new JsonObject();
		json.addProperty("id", "part");
		json.add("parent", parent.getJson());
		json.addProperty("x", textureU);
		json.addProperty("y", textureV);
		json.addProperty("width", subWidth);
		json.addProperty("height", subHeight);
		json.addProperty("corner", corner);
		json.addProperty("texture_width", textureWidth);
		json.addProperty("texture_height", textureHeight);
		return json;
	}
}
