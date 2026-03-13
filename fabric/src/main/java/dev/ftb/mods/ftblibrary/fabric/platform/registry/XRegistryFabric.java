package dev.ftb.mods.ftblibrary.fabric.platform.registry;

import dev.ftb.mods.ftblibrary.platform.registry.XRegistry;
import dev.ftb.mods.ftblibrary.platform.registry.XRegistryRef;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class XRegistryFabric<T> implements XRegistry<T> {
    private final String modId;

    private final Holder.Reference<Registry<T>> backingRegistry;
    private final List<XRegistryRef<T>> entries = new LinkedList<>();

    @SuppressWarnings("unchecked")
    public XRegistryFabric(String modId, ResourceKey<Registry<T>> backingRegistry) {
        this.modId = modId;
        this.backingRegistry = (Holder.Reference<Registry<T>>) BuiltInRegistries.REGISTRY.get(backingRegistry.identifier()).orElseThrow();
    }

    @Override
    public void init() {
        for (XRegistryRef<T> entry : entries) {
            Registry.register(backingRegistry.value(), entry.identifier(), entry.get());
        }
    }

    @Override
    public XRegistryRef<T> register(String id, Supplier<T> value) {
        var identifier = Identifier.fromNamespaceAndPath(this.modId, id);
        var entry = new XRegistryRefFabric<>(this.backingRegistry.key(), identifier, value);
        entries.add(entry);
        return entry;
    }
}
