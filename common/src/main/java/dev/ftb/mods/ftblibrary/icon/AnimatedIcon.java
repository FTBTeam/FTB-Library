package dev.ftb.mods.ftblibrary.icon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ftb.mods.ftblibrary.client.icon.AnimatedIconRenderer;
import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AnimatedIcon extends Icon<AnimatedIcon> {
    private final List<Icon<?>> list;

    private AnimatedIcon(List<Icon<?>> list) {
        this.list = list;
    }

    public List<Icon<?>> getList() {
        return list;
    }

    public static Icon<?> fromList(List<Icon<?>> icons, boolean includeEmpty) {
        List<Icon<?>> list = new ArrayList<>(icons.size());

        for (var icon : icons) {
            if (icon instanceof AnimatedIcon a) {
                for (var icon1 : a.list) {
                    if (includeEmpty || !icon1.isEmpty()) {
                        list.add(icon1);
                    }
                }
            } else if (includeEmpty || !icon.isEmpty()) {
                list.add(icon);
            }
        }

        if (list.isEmpty()) {
            return empty();
        } else if (list.size() == 1) {
            return list.getFirst();
        } else {
            return new AnimatedIcon(list);
        }
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public JsonElement getJson() {
        return Util.make(new JsonObject(), j -> {
            j.addProperty("id", "animation");
            j.add("icons", Util.make(new JsonArray(), a -> list.stream().map(Icon::getJson).forEach(a::add)));
        });
    }

    public int hashCode() {
        return list.hashCode();
    }

    public boolean equals(Object o) {
        return o == this || o instanceof AnimatedIcon && list.equals(((AnimatedIcon) o).list);
    }

    @Override
    @Nullable
    public Object getIngredient() {
        if (!list.isEmpty()) {
            return list.get((int) ((System.currentTimeMillis() / 1000L) % list.size())).getIngredient();
        }

        return null;
    }

    @Override
    public IconRenderer<AnimatedIcon> getRenderer() {
        return AnimatedIconRenderer.INSTANCE;
    }
}
