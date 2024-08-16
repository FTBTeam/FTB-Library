package dev.ftb.mods.ftblibrary.icon;

import org.jetbrains.annotations.Nullable;


@FunctionalInterface
public interface ImageCallback<T> {
    void imageLoaded(boolean queued, @Nullable T image);
}
