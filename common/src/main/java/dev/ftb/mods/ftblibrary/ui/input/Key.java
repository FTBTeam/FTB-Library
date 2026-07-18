package dev.ftb.mods.ftblibrary.ui.input;

import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

public class Key {
    public final int keyCode, scanCode;
    public final KeyModifiers modifiers;

    public Key(int keyCode, int scanCode, int modifiers) {
        this.keyCode = keyCode;
        this.scanCode = scanCode;
        this.modifiers = new KeyModifiers(modifiers);
    }

    public boolean matches(KeyMapping keyMapping) {
        return ClientUtils.input().matches(keyMapping, keyCode, scanCode);
    }

    public boolean is(int k) {
        return keyCode == k;
    }

    public InputConstants.Key getInputMapping() {
        return InputConstants.getKey(keyCode, scanCode);
    }

    public boolean esc() {
        return is(GLFW.GLFW_KEY_ESCAPE);
    }

    public boolean escOrInventory() {
        return esc() || matches(Minecraft.getInstance().options.keyInventory);
    }

    public boolean enter() {
        return is(GLFW.GLFW_KEY_ENTER);
    }

    public boolean backspace() {
        return is(GLFW.GLFW_KEY_BACKSPACE);
    }

    public boolean cut() {
        return Screen.isCut(keyCode);
    }

    public boolean paste() {
        return Screen.isPaste(keyCode);
    }

    public boolean copy() {
        return Screen.isCopy(keyCode);
    }

    public boolean selectAll() {
        return Screen.isSelectAll(keyCode);
    }

    public boolean deselectAll() {
        return keyCode == GLFW.GLFW_KEY_D && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }
}
