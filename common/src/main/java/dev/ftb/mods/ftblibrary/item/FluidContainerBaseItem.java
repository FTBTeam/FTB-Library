package dev.ftb.mods.ftblibrary.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public abstract class FluidContainerBaseItem extends Item {
	public FluidContainerBaseItem() {
		super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC));
	}
}
