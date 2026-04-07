package dev.ftb.mods.ftblibrary.integration.platform;

import java.util.ServiceLoader;

public interface PlatformIntegrations {
    PlatformIntegrations INSTANCE = ServiceLoader.load(PlatformIntegrations.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No platform integrations found!"));

    JeiShim jei();
}
