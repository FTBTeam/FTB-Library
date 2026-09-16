package dev.ftb.mods.ftblibrary.client.gui.input;


import org.lwjgl.sdl.SDLKeycode;

public class KeyModifiers {
    public static final KeyModifiers NONE = new KeyModifiers(0);

    public final int modifiers;

    public KeyModifiers(int m) {
        modifiers = m;
    }

    public boolean shift() {
        return (modifiers & SDLKeycode.SDL_KMOD_SHIFT) != 0;
    }

    public boolean control() {
        return (modifiers & SDLKeycode.SDL_KMOD_CTRL) != 0;
    }

    public boolean alt() {
        return (modifiers & SDLKeycode.SDL_KMOD_ALT) != 0;
    }

    public boolean start() {
        return (modifiers & SDLKeycode.SDL_KMOD_GUI) != 0;
    }

    public boolean numLock() {
        return (modifiers & SDLKeycode.SDL_KMOD_NUM) != 0;
    }

    public boolean capsLock() {
        return (modifiers & SDLKeycode.SDL_KMOD_CAPS) != 0;
    }

    public boolean onlyControl() {
        return control() && !shift() && !alt();
    }
}
