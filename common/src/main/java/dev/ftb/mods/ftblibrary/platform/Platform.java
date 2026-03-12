package dev.ftb.mods.ftblibrary.platform;

import dev.ftb.mods.ftblibrary.platform.network.NetworkingShim;

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

    Mod getMod(String modId);

    default Optional<Mod> getModOptional(String modId) {
        return Optional.ofNullable(get().getMod(modId));
    }

    boolean isModLoaded(String modId);

    Collection<Mod> getMods();

    Misc misc();

    Paths paths();

    NetworkingShim networking();
}
