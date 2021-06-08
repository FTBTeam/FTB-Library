package dev.ftb.mods.ftblibrary.ui;

import org.lwjgl.glfw.GLFW;

public enum CursorType {
	ARROW(GLFW.GLFW_ARROW_CURSOR),
	IBEAM(GLFW.GLFW_IBEAM_CURSOR),
	CROSSHAIR(GLFW.GLFW_CROSSHAIR_CURSOR),
	HAND(GLFW.GLFW_HAND_CURSOR),
	HRESIZE(GLFW.GLFW_HRESIZE_CURSOR),
	VRESIZE(GLFW.GLFW_VRESIZE_CURSOR);

	public final long cursor;

	CursorType(long c) {
		cursor = c;
	}
}
