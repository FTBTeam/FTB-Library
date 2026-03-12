package dev.ftb.mods.ftblibrary.platform.client;

import java.util.ServiceLoader;

public interface PlatformClient {
    PlatformClient INSTANCE = ServiceLoader.load(PlatformClient.class).findFirst().orElseThrow();

    static PlatformClient get() {
        return INSTANCE;
    }
}
