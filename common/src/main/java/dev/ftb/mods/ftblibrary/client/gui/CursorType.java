package dev.ftb.mods.ftblibrary.client.gui;

import org.jspecify.annotations.Nullable;
import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.system.MemoryUtil;

public enum CursorType {
    // TODO: I do not believe any of these have been ported correctly. They didn't map properly
    ARROW(SDLMouse.SDL_SYSTEM_CURSOR_DEFAULT),
    IBEAM(SDLMouse.SDL_SYSTEM_CURSOR_TEXT),
    CROSSHAIR(SDLMouse.SDL_SYSTEM_CURSOR_CROSSHAIR),
    HAND(SDLMouse.SDL_SYSTEM_CURSOR_POINTER),
    HRESIZE(SDLMouse.SDL_SYSTEM_CURSOR_SE_RESIZE),
    VRESIZE(SDLMouse.SDL_SYSTEM_CURSOR_NE_RESIZE),
    MOVE(GLFW.GLFW_RESIZE_ALL_CURSOR);

    private final int shape;
    private long cursor = 0L;

    CursorType(int c) {
        shape = c;
    }

    public static void set(@Nullable CursorType type) {
//        var window = Minecraft.getInstance().getWindow().handle();

        if (type == null) {
            SDLMouse.SDL_SetCursor(MemoryUtil.NULL);
            return;
        }

        if (type.cursor == 0L) {
            type.cursor = SDLMouse.SDL_CreateSystemCursor(type.shape);
        }

        SDLMouse.SDL_SetCursor(type.cursor);
    }
}
