package dev.ftb.mods.ftblibrary.platform.registry;

import dev.ftb.mods.ftblibrary.platform.Platform;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

public interface XRegistry<T> {
    static <T> XRegistry<T> create(String modId, ResourceKey<Registry<T>> registryKey) {
        return Platform.get().createRegistry(modId, registryKey);
    }

    void init();

    XRegistryRef<T> register(String id, Supplier<T> value);
}
