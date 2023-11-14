package dev.ftb.mods.ftblibrary.ui;


public enum WidgetType {
	NORMAL,
	MOUSE_OVER,
	DISABLED;

	public static WidgetType mouseOver(boolean mouseOver) {
		return mouseOver ? MOUSE_OVER : NORMAL;
	}
}
