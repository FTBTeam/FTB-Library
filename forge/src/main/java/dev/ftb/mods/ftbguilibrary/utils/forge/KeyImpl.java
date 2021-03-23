package dev.ftb.mods.ftbguilibrary.utils.forge;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class KeyImpl {
	public static boolean matchesWithoutConflicts(KeyMapping keyBinding, InputConstants.Key keyCode) {
		return keyBinding.isActiveAndMatches(keyCode);
	}
}
