package dev.ftb.mods.ftblibrary.fabric.platform.registry;

import dev.ftb.mods.ftblibrary.platform.registry.XRegistryRef;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

public class XRegistryRefFabric<R, T extends R> implements XRegistryRef<T> {
    private final ResourceKey<T> resourceKey;
    private final Identifier identifier;
    private final Supplier<T> supplier;
    private T cachedValue;
    private boolean initialized;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public XRegistryRefFabric(ResourceKey<? extends Registry<R>> key, Identifier identifier, Supplier<T> supplier) {
        // This appears to have no nice way around it. We can trust the input so this is acceptable.
        this.resourceKey = ResourceKey.create((ResourceKey) key, identifier);
        this.identifier = identifier;
        this.supplier = supplier;
        this.initialized = false;
    }

    @Override
    public T get() {
        if (!initialized) {
            cachedValue = supplier.get();
            initialized = true;
        }
        return cachedValue;
    }

    @Override
    public Identifier identifier() {
        return identifier;
    }

    @Override
    public ResourceKey<T> resourceKey() {
        return resourceKey;
    }
}
