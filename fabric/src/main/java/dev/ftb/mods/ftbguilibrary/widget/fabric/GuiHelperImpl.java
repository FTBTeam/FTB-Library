package dev.ftb.mods.ftbguilibrary.widget.fabric;

import net.minecraft.client.gui.Font;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class GuiHelperImpl {
	public static Font getFont(ItemStack stack) {
		return null;
	}

	public static boolean shouldShowDurability(ItemStack stack) {
		return stack.isDamaged();
	}

	public static double getDamageLevel(ItemStack stack) {
		return (double) stack.getDamageValue() / (double) stack.getMaxDamage();
	}

	public static int getDurabilityColor(ItemStack stack) {
		float f = stack.getDamageValue();
		float g = stack.getMaxDamage();
		return Mth.hsvToRgb(Math.max(0.0F, (g - f) / g) / 3.0F, 1.0F, 1.0F);
	}

}
