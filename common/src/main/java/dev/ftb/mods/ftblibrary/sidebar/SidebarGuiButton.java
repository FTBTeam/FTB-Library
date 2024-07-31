package dev.ftb.mods.ftblibrary.sidebar;


import net.minecraft.resources.ResourceLocation;

public class SidebarGuiButton {

	private final ResourceLocation buttonId;
	public final SidebarButton button;
	public int x, y;
	private int gridX;
	private int gridY;
	private boolean enabled;

	public SidebarGuiButton(int gridX, int gridY, boolean enabled, ResourceLocation buttonId, SidebarButton b) {
		button = b;
		x = 0;
		y = 0;
		this.gridX = gridX;
		this.gridY = gridY;
		this.buttonId = buttonId;
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public ResourceLocation getButtonId() {
		return buttonId;
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
