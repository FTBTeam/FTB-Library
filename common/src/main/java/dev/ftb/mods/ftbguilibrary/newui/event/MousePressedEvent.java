package dev.ftb.mods.ftbguilibrary.newui.event;

import dev.ftb.mods.ftbguilibrary.utils.MouseButton;

/**
 * @author LatvianModder
 */
public class MousePressedEvent extends MouseEvent {
	public final MouseButton button;

	public MousePressedEvent(double _x, double _y, MouseButton b) {
		super(_x, _y);
		button = b;
	}
}