package dev.ftb.mods.ftblibrary.neoforge.platform.registry;

import dev.ftb.mods.ftblibrary.platform.registry.XRegistry;
import dev.ftb.mods.ftblibrary.platform.registry.XRegistryRef;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class XRegistryNeo<T> implements XRegistry<T> {
    private final DeferredRegister<T> backingRegistry;
    private final String modId;

    public XRegistryNeo(String modId, ResourceKey<Registry<T>> registryKey) {
        this.backingRegistry = DeferredRegister.create(registryKey, modId);
        this.modId = modId;
    }

    @Override
    public void init() {
        var modBus = ModList.get().getModContainerById(this.modId)
                .map(ModContainer::getEventBus);

        backingRegistry.register(modBus.orElseThrow());
    }

    @Override
    public <I extends T> XRegistryRef<I> register(String id, Supplier<I> value) {
        var identifier = Identifier.fromNamespaceAndPath(this.modId, id);
        var entry = backingRegistry.register(id, value);

        return new XRegistryRef<I>() {
            @Override
            public Identifier identifier() {
                return identifier;
            }

            @SuppressWarnings("unchecked")
            @Override
            public ResourceKey<I> resourceKey() {
                return (ResourceKey<I>) entry.getKey();
            }

            @Override
            public I get() {
                return entry.get();
            }
        };
    }
}
