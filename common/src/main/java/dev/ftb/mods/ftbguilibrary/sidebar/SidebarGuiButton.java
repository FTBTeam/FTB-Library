package dev.ftb.mods.ftbguilibrary.sidebar;

/**
 * @author LatvianModder
 */
public class SidebarGuiButton {
	public final int buttonX, buttonY;
	public final SidebarButton button;
	public int x, y;

	public SidebarGuiButton(int x, int y, SidebarButton b) {
		buttonX = x;
		buttonY = y;
		button = b;
	}
}