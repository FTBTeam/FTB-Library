package dev.ftb.mods.ftblibrary.icon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.ftb.mods.ftblibrary.client.icon.CombinedIconRenderer;
import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class CombinedIcon extends Icon<CombinedIcon> {
    public final List<Icon<?>> list;

    CombinedIcon(Collection<Icon<?>> icons) {
        list = new ArrayList<>(icons.size());

        for (var icon : icons) {
            // not necessarily safe to query a LazyIcon at this point
            // e.g. combined icon containing a lazy item icon loaded from a sidebar json would cause a crash
            if (icon instanceof LazyIcon || !icon.isEmpty()) {
                list.add(icon);
            }
        }
    }

    CombinedIcon(Icon<?> o1, Icon<?> o2) {
        list = new ArrayList<>(List.of(o1, o2));
    }

    public static Icon<?> getCombined(Collection<Icon<?>> icons) {
        List<Icon<?>> list = new ArrayList<>(icons.size());

        for (var icon : icons) {
            // see above
            if (icon instanceof LazyIcon || !icon.isEmpty()) {
                list.add(icon);
            }
        }

        if (list.isEmpty()) {
            return empty();
        } else if (list.size() == 1) {
            return list.getFirst();
        }

        return new CombinedIcon(list);
    }

    @Override
    public JsonElement getJson() {
        var json = new JsonArray();

        for (var o : list) {
            json.add(o.getJson());
        }

        return json;
    }

    public int hashCode() {
        return list.hashCode();
    }

    public boolean equals(Object o) {
        return o == this || o instanceof CombinedIcon && list.equals(((CombinedIcon) o).list);
    }

    @Override
    public IconRenderer<CombinedIcon> getRenderer() {
        return CombinedIconRenderer.INSTANCE;
    }
}
