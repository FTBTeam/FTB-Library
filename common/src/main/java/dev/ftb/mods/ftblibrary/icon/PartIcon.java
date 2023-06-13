package dev.ftb.mods.ftblibrary.icon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;

/**
 * @author LatvianModder
 */
public class PartIcon extends IconWithParent {
	public final Icon parent;
	public int textureWidth, textureHeight;
	public int posX, posY, corner, width, height;

	private Icon all, middleU, middleD, middleL, middleR, cornerNN, cornerPN, cornerNP, cornerPP, center;

	public PartIcon(Icon icon, int x, int y, int w, int h, int c) {
		super(icon);
		parent = icon;
		textureWidth = 256;
		textureHeight = 256;
		posX = x;
		posY = y;
		width = w;
		height = h;
		corner = c;
		updateParts();
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
		return parent.withUV(posX + x, posY + y, w, h, textureWidth, textureHeight);
	}

	public void updateParts() {
		var mw = width - corner * 2;
		var mh = height - corner * 2;
		all = get(0, 0, width, height);
		middleU = get(corner, 0, mw, corner);
		middleD = get(corner, height - corner, mw, corner);
		middleL = get(0, corner, corner, mh);
		middleR = get(width - corner, corner, corner, mh);
		cornerNN = get(0, 0, corner, corner);
		cornerPN = get(width - corner, 0, corner, corner);
		cornerNP = get(0, height - corner, corner, corner);
		cornerPP = get(width - corner, height - corner, corner, corner);
		center = get(corner, corner, mw, mh);
	}

	@Override
	public PartIcon copy() {
		var icon = new PartIcon(parent.copy());
		icon.posX = posX;
		icon.posY = posY;
		icon.width = width;
		icon.height = height;
		icon.corner = corner;
		icon.textureWidth = textureWidth;
		icon.textureHeight = textureHeight;
		return icon;
	}

	@Override
	protected void setProperties(IconProperties properties) {
		super.setProperties(properties);
		posX = properties.getInt("x", posX);
		posY = properties.getInt("y", posY);
		width = properties.getInt("width", height);
		height = properties.getInt("height", height);
		corner = properties.getInt("corner", corner);
		textureWidth = properties.getInt("texture_w", textureWidth);
		textureHeight = properties.getInt("texture_h", textureHeight);

		var s = properties.getString("pos", "");

		if (!s.isEmpty()) {
			var s1 = s.split(",", 4);

			if (s1.length == 4) {
				posX = Integer.parseInt(s1[0]);
				posY = Integer.parseInt(s1[1]);
				width = Integer.parseInt(s1[2]);
				height = Integer.parseInt(s1[3]);
			}
		}

		updateParts();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
		if (w == width && h == height) {
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
		json.addProperty("x", posX);
		json.addProperty("y", posY);
		json.addProperty("width", width);
		json.addProperty("height", height);
		json.addProperty("corner", corner);
		json.addProperty("texture_width", textureWidth);
		json.addProperty("texture_height", textureHeight);
		return json;
	}
}