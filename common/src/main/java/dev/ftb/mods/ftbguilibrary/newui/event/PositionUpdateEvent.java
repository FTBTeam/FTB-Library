package dev.ftb.mods.ftbguilibrary.newui.event;

import dev.ftb.mods.ftbguilibrary.newui.Widget;

/**
 * @author LatvianModder
 */
public class PositionUpdateEvent {
	public final double mouseX;
	public final double mouseY;
	public Widget widgetUnderMouse;

	public PositionUpdateEvent(double mx, double my) {
		mouseX = mx;
		mouseY = my;
	}

	public boolean mouseOver(double px, double py, double w, double h) {
		return mouseX >= px && mouseY >= py && mouseX < px + w && mouseY < py + h;
	}
}