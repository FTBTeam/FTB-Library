package dev.ftb.mods.ftblibrary.icon;

import com.google.gson.JsonElement;
import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import dev.ftb.mods.ftblibrary.client.icon.LazyIconRenderer;
import dev.ftb.mods.ftblibrary.util.Lazy;
import org.jspecify.annotations.Nullable;

public class LazyIcon extends Icon<LazyIcon> {
    public final Lazy<Icon<?>> iconSupplier;

    public LazyIcon(Lazy<Icon<?>> iconSupplier) {
        this.iconSupplier = iconSupplier;
    }

    public Icon<?> getDelegate() {
        return iconSupplier.get();
    }

    @Override
    public boolean isEmpty() {
        return getDelegate().isEmpty();
    }

    @Override
    public LazyIcon copy() {
        return new LazyIcon(Lazy.of(() -> getDelegate().copy()));
    }

    @Override
    public JsonElement getJson() {
        return getDelegate().getJson();
    }

    public int hashCode() {
        return getJson().hashCode();
    }

    @Override
    @Nullable
    public Object getIngredient() {
        return getDelegate().getIngredient();
    }

    @Override
    protected void setProperties(IconProperties properties) {
        getDelegate().setProperties(properties);
    }

    @Override
    public IconRenderer<LazyIcon> getRenderer() {
        return LazyIconRenderer.INSTANCE;
    }

    @Override
    public String toString() {
        return getDelegate().toString();
    }
}
