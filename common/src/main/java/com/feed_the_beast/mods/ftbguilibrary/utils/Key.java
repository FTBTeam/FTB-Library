package com.feed_the_beast.mods.ftbguilibrary.utils;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * @author LatvianModder
 */
public class Key {
    public final int keyCode, scanCode;
    public final KeyModifiers modifiers;

    public Key(int k, int s, int m) {
        keyCode = k;
        scanCode = s;
        modifiers = new KeyModifiers(m);
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
        return esc();
        /* FIXME: reimpl forge patch */
        // return esc() || Minecraft.getInstance().options.keyInventory.isActiveAndMatches(getInputMapping());
    }

    public boolean enter() {
        return is(GLFW.GLFW_KEY_ENTER);
    }

    public boolean backspace() {
        return is(GLFW.GLFW_KEY_BACKSPACE);
    }

    public boolean cut() {
        return is(GLFW.GLFW_KEY_X) && modifiers.onlyControl();
    }

    public boolean paste() {
        return is(GLFW.GLFW_KEY_V) && modifiers.onlyControl();
    }

    public boolean copy() {
        return is(GLFW.GLFW_KEY_C) && modifiers.onlyControl();
    }

    public boolean selectAll() {
        return is(GLFW.GLFW_KEY_A) && modifiers.onlyControl();
    }

    public boolean deselectAll() {
        return is(GLFW.GLFW_KEY_D) && modifiers.onlyControl();
    }
}