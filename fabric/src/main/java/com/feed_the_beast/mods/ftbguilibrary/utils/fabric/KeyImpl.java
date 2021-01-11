package com.feed_the_beast.mods.ftbguilibrary.utils.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class KeyImpl
{
	public static boolean matchesWithoutConflicts(KeyMapping keyBinding, InputConstants.Key keyCode)
	{
		return keyCode != InputConstants.UNKNOWN && keyCode.equals(keyBinding.key);
	}
}
