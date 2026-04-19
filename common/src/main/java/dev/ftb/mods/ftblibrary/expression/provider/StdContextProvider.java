package dev.ftb.mods.ftblibrary.expression.provider;

import dev.ftb.mods.ftblibrary.platform.Platform;

/// Standard-library context provider, registered under the {@code "std"} key.
public class StdContextProvider extends ContextProvider {
    public StdContextProvider() {
        super("std");
    }

    /// Returns {@code true} if the mod with the given mod ID is currently loaded.
    public boolean isModLoaded(String modId) {
        return Platform.get().isModLoaded(modId);
    }
}
