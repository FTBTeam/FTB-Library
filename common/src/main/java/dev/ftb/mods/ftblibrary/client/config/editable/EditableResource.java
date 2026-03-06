package dev.ftb.mods.ftblibrary.client.config.editable;

import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectableResource;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.icon.Color4I;

import java.util.OptionalLong;
import java.util.function.Predicate;

public abstract class EditableResource<T> extends EditableConfigValue<T> {
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

    @Override
    public Color4I getColor(T value, Theme theme) {
        return theme.hasDarkBackground() ? Color4I.rgb(0xA0A0FF) : Color4I.rgb(0x202080);
    }

    public EditableResource<T> setAllowNBTEdit(boolean allow) {
        allowNBTEdit = allow;
        return this;
    }

    public EditableResource<T> withFilter(Predicate<T> filter) {
        this.filter = filter;
        return this;
    }

    public EditableResource<T> withAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
        return this;
    }

    public boolean allowResource(T resource) {
        return filter.test(resource);
    }
}
