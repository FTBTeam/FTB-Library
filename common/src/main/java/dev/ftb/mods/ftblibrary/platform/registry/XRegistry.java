package dev.ftb.mods.ftblibrary.platform.registry;

import dev.ftb.mods.ftblibrary.platform.Platform;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

/// Cross-platform registry object, abstracting Fabric and NeoForge object registration.
/// @param <T> the registry type
public interface XRegistry<T> {
    /// Create a cross-platform registry abstraction for a given object type. Create this as a static final
    /// object.
    ///
    /// @param modId your mod ID
    /// @param registryKey ID of the registry being abstracted (vanilla registries are all in [Registries])
    /// @return the registry abstraction
    /// @param <T> the object type
    static <T> XRegistry<T> create(String modId, ResourceKey<Registry<T>> registryKey) {
        return Platform.get().createRegistry(modId, registryKey);
    }

    /// Initialize the registry abstraction with the platform-specific implementation. Call this from your mod
    /// constructor.
    void init();

    /// Register a new object.
    ///
    /// @param id the registry ID of your object (automatically namespaced with the ID passed to [#create(String, ResourceKey)]
    /// @param value supplier of the object being registered
    /// @return a registry reference (in effect an object supplier)
    XRegistryRef<T> register(String id, Supplier<T> value);
}
