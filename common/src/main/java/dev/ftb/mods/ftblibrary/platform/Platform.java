package dev.ftb.mods.ftblibrary.platform;

import dev.ftb.mods.ftblibrary.platform.network.Networking;
import dev.ftb.mods.ftblibrary.platform.registry.XRegistry;
import dev.ftb.mods.ftblibrary.platform.transfer.Transfer;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.Collection;
import java.util.Map;
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

    Networking networking();

    Transfer transfer();

    void addDataPackReloadListeners(String modId, Map<Identifier, PreparableReloadListener> listeners);

    default void addDataPackReloadListener(String modId, Identifier id, PreparableReloadListener listener) {
        addDataPackReloadListeners(modId, Map.of(id, listener));
    }

    <T> XRegistry<T> createRegistry(String modId, ResourceKey<Registry<T>> registryKey);
}
