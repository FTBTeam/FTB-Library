package dev.ftb.mods.ftblibrary.fabric.integrations.platform;

import dev.ftb.mods.ftblibrary.integration.platform.JeiShim;
import dev.ftb.mods.ftblibrary.integration.platform.PlatformIntegrations;

public class PlatformIntegrationsFabric implements PlatformIntegrations {
    private final JeiShim jeiShim = new JeiShimFabric();

    @Override
    public JeiShim jei() {
        return jeiShim;
    }
}
