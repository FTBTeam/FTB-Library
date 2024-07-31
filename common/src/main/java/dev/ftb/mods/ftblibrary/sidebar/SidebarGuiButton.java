package dev.ftb.mods.ftblibrary.sidebar;


public class SidebarGuiButton {

	public final SidebarButton button;
	public int x, y;
	private int gridX;
	private int gridY;

	public SidebarGuiButton(int gridX, int gridY, SidebarButton b) {
		button = b;
		x = 0;
		y = 0;
		this.gridX = gridX;
		this.gridY = gridY;
	}

	public int getGridX() {
		return gridX;
	}

	public int getGridY() {
		return gridY;
	}

	public void setGridX(int gridX) {
		this.gridX = gridX;
	}

	public void setGridY(int gridY) {
		this.gridY = gridY;
	}

	public void setGrid(int gridX, int gridY) {
		this.gridX = gridX;
		this.gridY = gridY;
	}
}
