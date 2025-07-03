package dev.ftb.mods.ftblibrary.sidebar;

public class SidebarGuiButton {
    private final RegisteredSidebarButton sidebarButton;

    public int x, y;
    private GridLocation gridLocation;
    private boolean enabled;

    public SidebarGuiButton(GridLocation gridLocation, boolean enabled, RegisteredSidebarButton sidebarButton) {
        x = 0;
        y = 0;
        this.gridLocation = gridLocation;
        this.sidebarButton = sidebarButton;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public RegisteredSidebarButton getSidebarButton() {
        return sidebarButton;
    }

    public GridLocation getGridLocation() {
        return gridLocation;
    }

    public void setGridLocation(GridLocation gridLocation) {
        this.gridLocation = gridLocation;
    }

    public void setGridLocation(int x, int y) {
        this.gridLocation = new GridLocation(x, y);
    }

    @Override
    public String toString() {
        return sidebarButton.getId().toString();
    }
}
