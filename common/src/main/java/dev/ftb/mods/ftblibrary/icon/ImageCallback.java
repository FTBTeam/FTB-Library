package dev.ftb.mods.ftblibrary.icon;

import org.jspecify.annotations.Nullable;


@FunctionalInterface
public interface ImageCallback<T> {
    void imageLoaded(boolean queued, @Nullable T image);
}
