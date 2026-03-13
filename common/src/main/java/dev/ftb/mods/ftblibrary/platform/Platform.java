package dev.ftb.mods.ftblibrary.platform;

import dev.ftb.mods.ftblibrary.platform.network.NetworkingShim;
import dev.ftb.mods.ftblibrary.platform.registry.XRegistry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.Collection;
import java.util.Optional;
import java.util.ServiceLoader;

public interface Platform {
    Platform INSTANCE = ServiceLoader.load(Platform.class).findFirst().orElseThrow();

    static Platform get() {
        return INSTANCE;
    }

    Env env();

    boolean isDev();

    boolean isFabric();

    boolean isNeoForge();

    Optional<Mod> getMod(String modId);

    boolean isModLoaded(String modId);

    Collection<Mod> getMods();

    Misc misc();

    Paths paths();

    NetworkingShim networking();

    <T> XRegistry<T> createRegistry(String modId, ResourceKey<Registry<T>> registryKey);
}
