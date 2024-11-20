package dev.ftb.mods.ftblibrary.icon;

import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;


public class IconProperties {
    private final Map<String, String> map = new LinkedHashMap<>();

    public void set(String key, String value) {
        map.remove(key);
        map.put(key, value);
    }

    public String getString(String key, String def) {
        return map.getOrDefault(key, def);
    }

    public int getInt(String key, int def, int min, int max) {
        if (map.containsKey(key)) {
            return Mth.clamp(Integer.parseInt(map.get(key)), min, max);
        }

        return def;
    }

    public int getInt(String key, int def) {
        return getInt(key, def, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public double getDouble(String key, double def, double min, double max) {
        if (map.containsKey(key)) {
            return Mth.clamp(Double.parseDouble(map.get(key)), min, max);
        }

        return def;
    }

    public double getDouble(String key, double def) {
        return getDouble(key, def, -Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public boolean getBoolean(String key, boolean def) {
        return map.containsKey(key) ? map.get(key).equals("1") : def;
    }

    @Nullable
    public Color4I getColor(String key) {
        var s = map.get(key);
        return s == null ? null : Color4I.fromString(s);
    }
}
