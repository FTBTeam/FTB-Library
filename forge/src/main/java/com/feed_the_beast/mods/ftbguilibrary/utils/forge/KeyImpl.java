package com.feed_the_beast.mods.ftbguilibrary.utils.forge;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.architectury.ExpectPlatform;
import net.minecraft.client.KeyMapping;

public class KeyImpl
{
	public static boolean matchesWithoutConflicts(KeyMapping keyBinding, InputConstants.Key keyCode) {
		return keyBinding.isActiveAndMatches(keyCode);
	}
}
