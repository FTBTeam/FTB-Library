package dev.ftb.mods.ftblibrary.ui.input.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ftb.mods.ftblibrary.core.mixin.fabric.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;

public class KeyImpl {
	public static boolean matchesWithoutConflicts(KeyMapping keyBinding, InputConstants.Key keyCode) {
		return keyCode != InputConstants.UNKNOWN && keyCode.equals(((KeyMappingAccessor) keyBinding).getKey());
	}
}
