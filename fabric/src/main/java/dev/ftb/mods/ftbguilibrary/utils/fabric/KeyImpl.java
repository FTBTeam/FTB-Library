package dev.ftb.mods.ftbguilibrary.utils.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ftb.mods.ftbguilibrary.fabric.mixin.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;

public class KeyImpl {
	public static boolean matchesWithoutConflicts(KeyMapping keyBinding, InputConstants.Key keyCode) {
		return keyCode != InputConstants.UNKNOWN && keyCode.equals(((KeyMappingAccessor) keyBinding).getKey());
	}
}
