package dev.ftb.mods.ftblibrary.util.forge;

import com.mojang.blaze3d.platform.GlStateManager;

public class ClientUtilsImpl {
	public static float getLastBrightnessX() {
		return GlStateManager.lastBrightnessX;
	}

	public static float getLastBrightnessY() {
		return GlStateManager.lastBrightnessY;
	}
}
