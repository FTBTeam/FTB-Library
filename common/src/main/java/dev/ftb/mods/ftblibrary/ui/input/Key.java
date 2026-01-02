package dev.ftb.mods.ftblibrary.ui.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

/**
 * TODO: @since 1.21.11
 *       This should really be removed / reduced as most of this data is now part of minecrafts KeyEvent
 */
public record Key(int keyCode, int scanCode, KeyModifiers modifiers, KeyEvent originalEvent) {
    public Key(int keyCode, int scanCode, int modifiers, KeyEvent originalEvent) {
        this(keyCode, scanCode, new KeyModifiers(modifiers), originalEvent);
    }

    @ExpectPlatform
    private static boolean matchesWithoutConflicts(KeyMapping keyBinding, InputConstants.Key keyCode) {
        throw new AssertionError();
    }

    public boolean is(int k) {
        return keyCode == k;
    }

    public InputConstants.Key getInputMapping() {
        return InputConstants.getKey(originalEvent);
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
        return originalEvent.isCut();
    }

    public boolean paste() {
        return originalEvent.isPaste();
    }

    public boolean copy() {
        return originalEvent.isCopy();
    }

    public boolean selectAll() {
        return originalEvent.isSelectAll();
    }

    public boolean deselectAll() {
        return keyCode == GLFW.GLFW_KEY_D && originalEvent.hasControlDown() && !originalEvent.hasShiftDown() && !originalEvent.hasAltDown();
    }
}
