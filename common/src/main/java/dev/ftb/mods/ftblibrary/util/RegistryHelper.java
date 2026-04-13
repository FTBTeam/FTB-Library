package dev.ftb.mods.ftblibrary.util;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

/**
 * Miscellaneous helper methods for working with registries.
 */
public class RegistryHelper {
    /**
     * Attempts to get the identifier for the given target from the given registry. Returns null if the registry is not present or the target is not registered.
     *
     * @param target The object to get the identifier for
     * @param registry The registry to look up the target in
     * @return The identifier for the target, or null if the registry is not present or the target is not registered
     * @param <T> The type of the target and registry, almost always inferred
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> Identifier getIdentifier(T target, ResourceKey<Registry<T>> registry) {
        Registry<T> lookUp = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(registry.identifier());
        if (lookUp == null) {
            return null;
        }

        return lookUp.getKey(target);
    }

    /**
     * Attempts to get the registry for the given registry key. Returns null if the registry is not present.
     * @param registry The registry to look up
     * @return The registry, or null if the registry is not present
     * @param <T> The type of the registry
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> Registry<T> getRegistry(ResourceKey<Registry<T>> registry) {
        return (Registry<T>) BuiltInRegistries.REGISTRY.getValue(registry.identifier());
    }
}
