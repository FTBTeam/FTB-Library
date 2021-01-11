package com.feed_the_beast.mods.ftbguilibrary.widget.forge;

import me.shedaniel.architectury.ExpectPlatform;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;

public class GuiHelperImpl
{
	public static Font getFont(ItemStack stack)
	{
		return stack.getItem().getFontRenderer(stack);
	}

	public static boolean shouldShowDurability(ItemStack stack)
	{
		return stack.getItem().showDurabilityBar(stack);
	}

	public static double getDamageLevel(ItemStack stack)
	{
		return stack.getItem().getDurabilityForDisplay(stack);
	}

	public static int getDurabilityColor(ItemStack stack)
	{
		return stack.getItem().getRGBDurabilityForDisplay(stack);
	}

}
