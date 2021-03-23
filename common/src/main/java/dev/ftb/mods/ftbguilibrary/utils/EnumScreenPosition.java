package dev.ftb.mods.ftbguilibrary.utils;

import net.minecraft.util.StringRepresentable;

/**
 * @author LatvianModder
 */
public enum EnumScreenPosition implements StringRepresentable {
	CENTER("center", 0, 0),
	TOP("top", 0, -1),
	BOTTOM("bottom", 0, 1),
	LEFT("left", -1, 0),
	RIGHT("right", 1, 0),
	TOP_LEFT("top_left", -1, -1),
	TOP_RIGHT("top_right", 1, -1),
	BOTTOM_LEFT("bottom_left", -1, 1),
	BOTTOM_RIGHT("bottom_right", 1, 1);

	private final String name;
	public final int offsetX, offsetY;

	EnumScreenPosition(String n, int ox, int oy) {
		name = n;
		offsetX = ox;
		offsetY = oy;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public int getX(int screenWidth, int width, int offset) {
		switch (offsetX) {
			case -1:
				return offset;
			case 1:
				return (screenWidth - width) / 2;
			default:
				return screenWidth - width - offset;
		}
	}

	public int getY(int screenHeight, int height, int offset) {
		switch (offsetY) {
			case -1:
				return offset;
			case 1:
				return (screenHeight - height) / 2;
			default:
				return screenHeight - height - offset;
		}
	}
}