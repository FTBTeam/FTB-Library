package dev.ftb.mods.ftblibrary.client.gui.input;

import dev.ftb.mods.ftblibrary.platform.Platform;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

public record Key(KeyEvent event) {
    public boolean is(int k) {
        return event.key() == k;
    }

    public InputConstants.Key getInputMapping() {
        return InputConstants.getKey(event);
    }

    public boolean esc() {
        return event.isEscape();
    }

    public boolean escOrInventory() {
        // arch expect
        //noinspection ConstantValue
        return esc() || matchesWithoutConflicts(Minecraft.getInstance().options.keyInventory, getInputMapping());
    }

    public boolean enter() {
        return is(GLFW.GLFW_KEY_ENTER);
    }

    public boolean backspace() {
        return is(GLFW.GLFW_KEY_BACKSPACE);
    }

    public boolean cut() {
        return event.isCut();
    }

    public boolean paste() {
        return event.isPaste();
    }

    public boolean copy() {
        return event.isCopy();
    }

    public boolean selectAll() {
        return event.isSelectAll();
    }

    public boolean deselectAll() {
        return is(GLFW.GLFW_KEY_D) && event.hasControlDown() && !event.hasShiftDown() && !event.hasAltDown();
    }

    public KeyModifiers modifiers() {
        return new KeyModifiers(event.modifiers());
    }

    private static boolean matchesWithoutConflicts(KeyMapping keyBinding, InputConstants.Key keyCode) {
        return Platform.get().misc().matchesWithoutConflicts(keyBinding, keyCode);
    }
}
