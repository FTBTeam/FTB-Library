package dev.ftb.mods.ftblibrary.integration.permissions;

import dev.ftb.mods.ftblibrary.FTBLibrary;

public enum PermissionHelper {
    INSTANCE;

    private static final PermissionProvider FALLBACK_PROVIDER = new FallbackPermissionProvider();

    private PermissionProvider activeImpl = null;

    public PermissionHelper getInstance() {
        return INSTANCE;
    }

    public void setProviderImpl(PermissionProvider newProvider) {
        if (activeImpl != null) {
            FTBLibrary.LOGGER.warn("Overriding existing permissions provider: {} -> {}", activeImpl.getName(), newProvider.getName());
        } else {
            FTBLibrary.LOGGER.info("Setting permissions provider implementation to: {}", newProvider.getName());
        }
        activeImpl = newProvider;
    }

    public PermissionProvider getProvider() {
        return activeImpl;
    }
}
