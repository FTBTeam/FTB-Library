package dev.ftb.mods.ftbguilibrary.newui.event;

/**
 * @author LatvianModder
 */
public class MouseEvent {
	public final double x;
	public final double y;

	public MouseEvent(double _x, double _y) {
		x = _x;
		y = _y;
	}

	public boolean mouseOver(double px, double py, double w, double h) {
		return x >= px && y >= py && x < px + w && y < py + h;
	}
}