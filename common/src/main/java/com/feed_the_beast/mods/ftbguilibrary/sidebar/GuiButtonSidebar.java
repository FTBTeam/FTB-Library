package com.feed_the_beast.mods.ftbguilibrary.sidebar;

/**
 * @author LatvianModder
 */
public class GuiButtonSidebar {
    public final int buttonX, buttonY;
    public final SidebarButton button;
    public int x, y;

    public GuiButtonSidebar(int x, int y, SidebarButton b) {
        buttonX = x;
        buttonY = y;
        button = b;
    }
}