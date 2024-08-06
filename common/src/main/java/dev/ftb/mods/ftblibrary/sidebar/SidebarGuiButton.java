package dev.ftb.mods.ftblibrary.sidebar;

public class SidebarGuiButton {

	private final SidebarButton sidebarButton;
	public int x, y;
	private GridLocation gridLocation;
	private boolean enabled;

	public SidebarGuiButton(GridLocation girdLocation, boolean enabled, SidebarButton sidebarButton) {
		x = 0;
		y = 0;
		this.gridLocation = girdLocation;
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
		return gridLocation;
	}

	public void setGridLocation(GridLocation gridLocation) {
		this.gridLocation = gridLocation;
	}

	public void setGridLocation(int x, int y) {
		this.gridLocation = new GridLocation(x, y);
	}

}
