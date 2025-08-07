package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.config.ui.SelectableResource;

import java.util.OptionalLong;
import java.util.function.Predicate;

public abstract class ResourceConfigValue<T> extends ConfigValue<T> {
    private boolean allowNBTEdit = true;
    private Predicate<T> filter = s -> true;

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

    public ResourceConfigValue<T> withFilter(Predicate<T> filter) {
        this.filter = filter;
        return this;
    }

    public boolean allowResource(T resource) {
        return filter.test(resource);
    }
}
