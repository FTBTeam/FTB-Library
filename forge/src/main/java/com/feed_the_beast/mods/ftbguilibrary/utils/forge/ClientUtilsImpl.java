package com.feed_the_beast.mods.ftbguilibrary.utils.forge;

import com.mojang.blaze3d.platform.GlStateManager;

public class ClientUtilsImpl
{
	public static float getLastBrightnessX()
	{
		return GlStateManager.lastBrightnessX;
	}

	public static float getLastBrightnessY()
	{
		return GlStateManager.lastBrightnessY;
	}
}
