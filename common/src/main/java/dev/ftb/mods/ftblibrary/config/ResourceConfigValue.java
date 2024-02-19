package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.config.ui.SelectableResource;

import java.util.OptionalLong;

public abstract class ResourceConfigValue<T> extends ConfigValue<T> {
    private boolean allowNBTEdit = true;

    public abstract boolean allowEmptyResource();

    public abstract OptionalLong fixedResourceSize();

    public abstract boolean isEmpty();

    public abstract SelectableResource<T> getResource();

    public abstract boolean setResource(SelectableResource<T> selectedStack);

    public boolean canHaveNBT() {
        return allowNBTEdit;
    }

    public ResourceConfigValue<T> setAllowNBTEdit(boolean allow) {
        allowNBTEdit = allow;
        return this;
    }
}
