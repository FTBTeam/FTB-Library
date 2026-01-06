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
//        if (cachedIcon == null) {
//            cachedIcon = iconSupplier.get();
//
//            if (cachedIcon == null || cachedIcon.isEmpty()) {
//                cachedIcon = Icon.empty();
//            }
//        }
//
//        return cachedIcon;
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

//    @Override
//    public Icon<?> withColor(Color4I color) {
//        return getDelegate().withColor(color);
//    }

//    @Override
//    public Icon<?> withTint(Color4I color) {
//        return getDelegate().withTint(color);
//    }
//
//    @Override
//    public Icon<?> withUV(float u0, float v0, float u1, float v1) {
//        return getDelegate().withUV(u0, v0, u1, v1);
//    }

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
