package dev.ftb.mods.ftblibrary.config.value;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftblibrary.config.serializer.ConfigSerializer;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMapValue<T> extends BaseValue<Map<String, T>> {
    private final Codec<T> codec;

    protected AbstractMapValue(ConfigGroup parent, String key, Map<String, T> defaultValue, Codec<T> codec) {
        super(parent, key, defaultValue);
        this.codec = codec;
        super.set(new HashMap<>(defaultValue));
    }

    @Override
    public void write(ConfigSerializer serializer) {
        serializer.putMap(key, this, codec);
    }

    @Override
    public void read(ConfigSerializer serializer) {
        set(serializer.getMap(key, defaultValue, codec));
    }
}
