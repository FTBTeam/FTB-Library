package dev.ftb.mods.ftblibrary.sidebar;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class SidebarButtonGroup implements Comparable<SidebarButtonGroup> {
	private final ResourceLocation id;
	private final int y;
	private final List<SidebarButton> buttons;

	public SidebarButtonGroup(ResourceLocation _id, int _y) {
		id = _id;
		y = _y;
		buttons = new ArrayList<>();
	}

	public ResourceLocation getId() {
		return id;
	}

	public int getY() {
		return y;
	}

	public List<SidebarButton> getButtons() {
		return buttons;
	}

	@Override
	public int compareTo(SidebarButtonGroup group) {
		return getY() - group.getY();
	}
}
