package dev.ftb.mods.ftblibrary.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

public enum CursorType {
    ARROW(GLFW.GLFW_ARROW_CURSOR),
    IBEAM(GLFW.GLFW_IBEAM_CURSOR),
    CROSSHAIR(GLFW.GLFW_CROSSHAIR_CURSOR),
    HAND(GLFW.GLFW_HAND_CURSOR),
    HRESIZE(GLFW.GLFW_HRESIZE_CURSOR),
    VRESIZE(GLFW.GLFW_VRESIZE_CURSOR);

    private final int shape;
    private long cursor = 0L;

    CursorType(int c) {
        shape = c;
    }

    @Environment(EnvType.CLIENT)
    public static void set(@Nullable CursorType type) {
        var window = Minecraft.getInstance().getWindow().getWindow();

        if (type == null) {
            GLFW.glfwSetCursor(window, MemoryUtil.NULL);
            return;
        }

        if (type.cursor == 0L) {
            type.cursor = GLFW.glfwCreateStandardCursor(type.shape);
        }

        GLFW.glfwSetCursor(window, type.cursor);
    }
}
