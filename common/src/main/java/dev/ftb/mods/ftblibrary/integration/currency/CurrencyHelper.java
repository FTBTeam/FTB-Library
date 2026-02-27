package dev.ftb.mods.ftblibrary.integration.currency;

import dev.ftb.mods.ftblibrary.FTBLibrary;

import java.util.Objects;

public enum CurrencyHelper {
    INSTANCE;

    private CurrencyProvider activeImpl = null;

    public static CurrencyHelper getInstance() {
        return INSTANCE;
    }

    public void setActiveImpl(CurrencyProvider newProvider) {
        if (activeImpl != null) {
            FTBLibrary.LOGGER.warn("Overriding existing currency provider: {} -> {}", activeImpl.getName(), newProvider.getName());
        } else {
            FTBLibrary.LOGGER.info("Setting permissions currency implementation to: {}", newProvider.getName());
        }
        this.activeImpl = newProvider;
    }

    public CurrencyProvider getProvider() {
        return Objects.requireNonNullElse(activeImpl, FallbackCurrencyProvider.INSTANCE);
    }
}
