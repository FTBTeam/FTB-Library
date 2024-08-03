package dev.ftb.mods.ftblibrary.sidebar;

public class SidebarGuiButton {

	private final SidebarButton sidebarButton;
	public int x, y;
	private int gridX, gridY;
	private boolean enabled;

	public SidebarGuiButton(GridLocation girdLocation, boolean enabled, SidebarButton sidebarButton) {
		x = 0;
		y = 0;
		gridX = girdLocation.x();
		gridY = girdLocation.y();
		this.sidebarButton = sidebarButton;
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public SidebarButton getSidebarButton() {
		return sidebarButton;
	}

	public GridLocation getGirdLocation() {
		return new GridLocation(gridX, gridY);
	}

	public void setGridLocation(GridLocation gridLocation) {
		this.gridX = gridLocation.x();
		this.gridY = gridLocation.y();
	}

	public void setGridLocation(int x, int y) {
		this.gridX = x;
		this.gridY = y;
	}

}
