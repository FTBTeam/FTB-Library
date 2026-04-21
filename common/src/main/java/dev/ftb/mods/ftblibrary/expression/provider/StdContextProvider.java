package dev.ftb.mods.ftblibrary.expression.provider;

import dev.ftb.mods.ftblibrary.platform.Env;
import dev.ftb.mods.ftblibrary.platform.Platform;

/// Standard-library context provider, registered under the `std.` key.
public class StdContextProvider extends ContextProvider {
    public StdContextProvider() {
        super("std");
    }

    /// Returns [true] if the mod with the given mod ID is currently loaded.
    public boolean isModLoaded(String modId) {
        return Platform.get().isModLoaded(modId);
    }

    public boolean isDevelopment() {
        return Platform.get().isDev();
    }

    public boolean isClient() {
        return Platform.get().env() == Env.CLIENT;
    }

    public boolean isServer() {
        return Platform.get().env() == Env.SERVER;
    }

    public long epoch() {
        return System.currentTimeMillis();
    }
}
