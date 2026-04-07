package dev.ftb.mods.ftblibrary.neoforge.integrations.platform;

import dev.ftb.mods.ftblibrary.integration.platform.JeiShim;
import dev.ftb.mods.ftblibrary.integration.platform.PlatformIntegrations;

public class PlatformIntegrationsNeoForge implements PlatformIntegrations {
    private final JeiShim jeiShim = new JeiShimNeoForge();

    @Override
    public JeiShim jei() {
        return jeiShim;
    }
}
