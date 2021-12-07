package dev.ftb.mods.ftblibrary.ui.forge;

import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;

public class GuiHelperImpl {
	public static boolean shouldShowDurability(ItemStack stack) {
		return stack.getItem().isBarVisible(stack);
	}

	public static double getDamageLevel(ItemStack stack) {
		return stack.getItem().getDamage(stack);
	}

	public static int getDurabilityColor(ItemStack stack) {
		return stack.getItem().getBarColor(stack);
	}

}
