package dev.ftb.mods.ftblibrary.ui.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

/**
 * @author LatvianModder
 */
public class Key {
	@ExpectPlatform
	private static boolean matchesWithoutConflicts(KeyMapping keyBinding, InputConstants.Key keyCode) {
		throw new AssertionError();
	}

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
		return esc() || matchesWithoutConflicts(Minecraft.getInstance().options.keyInventory, getInputMapping());
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