package dev.ftb.mods.ftblibrary.sidebar;

import dev.ftb.mods.ftblibrary.util.ChainedBooleanSupplier;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class SidebarButtonGroup implements Comparable<SidebarButtonGroup> {
	private final ResourceLocation id;
	private final int y;
	private final boolean isPinned;
	private final List<SidebarButton> buttons;

	@Deprecated(forRemoval = true)
	public SidebarButtonGroup(ResourceLocation id, int y) {
		this(id, y, true);
	}

	public SidebarButtonGroup(ResourceLocation id, int y, boolean isPinned) {
		this.id = id;
		this.y = y;
		this.isPinned = isPinned;
		buttons = new ArrayList<>();
	}

	public ResourceLocation getId() {
		return id;
	}

	public String getLangKey() {
		return Util.makeDescriptionId("sidebar_group", id);
	}

	public boolean isPinned() {
		return isPinned;
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
