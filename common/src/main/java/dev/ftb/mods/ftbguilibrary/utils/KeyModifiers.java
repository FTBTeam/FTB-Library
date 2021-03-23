package dev.ftb.mods.ftbguilibrary.utils;

import org.lwjgl.glfw.GLFW;

/**
 * @author LatvianModder
 */
public class KeyModifiers {
	public static final KeyModifiers NONE = new KeyModifiers(0);

	public final int modifiers;

	public KeyModifiers(int m) {
		modifiers = m;
	}

	public boolean shift() {
		return (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
	}

	public boolean control() {
		return (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
	}

	public boolean alt() {
		return (modifiers & GLFW.GLFW_MOD_ALT) != 0;
	}

	public boolean start() {
		return (modifiers & GLFW.GLFW_MOD_SUPER) != 0;
	}

	public boolean numLock() {
		return (modifiers & GLFW.GLFW_MOD_NUM_LOCK) != 0;
	}

	public boolean capsLock() {
		return (modifiers & GLFW.GLFW_MOD_CAPS_LOCK) != 0;
	}

	public boolean onlyControl() {
		return control() && !shift() && !alt();
	}
}