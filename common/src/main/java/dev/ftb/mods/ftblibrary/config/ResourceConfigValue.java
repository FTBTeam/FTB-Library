package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.ui.resource.SelectableResource;
import net.minecraft.resources.Identifier;

import java.util.OptionalLong;
import java.util.function.Predicate;

public abstract class ResourceConfigValue<T> extends ConfigValue<T> {
    private boolean allowNBTEdit = true;
    private boolean allowEmpty = true;
    private Predicate<T> filter = s -> true;

    public boolean allowEmptyResource() {
        return allowEmpty;
    }

    public abstract OptionalLong fixedResourceSize();

    public abstract boolean isEmpty();

    public abstract SelectableResource<T> getResource();

    public abstract boolean setResource(SelectableResource<T> selectable);

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

    public ResourceConfigValue<T> withAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
        return this;
    }

    public boolean allowResource(T resource) {
        return filter.test(resource);
    }

    public static abstract class Image<T> extends ResourceConfigValue<T> {
        public static final Identifier NONE = FTBLibrary.rl("none");

        @Override
        public boolean canHaveNBT() {
            return false;
        }

        @Override
        public OptionalLong fixedResourceSize() {
            return OptionalLong.of(1);
        }

        @Override
        public boolean isEmpty() {
            return value == null || value.equals(NONE);
        }
    }
}
