package dev.ftb.mods.ftblibrary.client.gui.input.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class KeyImpl {
    public static boolean matchesWithoutConflicts(KeyMapping keyBinding, InputConstants.Key keyCode) {
        return keyBinding.isActiveAndMatches(keyCode);
    }
}
