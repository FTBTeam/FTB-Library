package dev.ftb.mods.ftblibrary.config.value;

import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;

public class StringMapValue extends AbstractMapValue<String> {
    public StringMapValue(ConfigGroup parent, String key, Map<String, String> defaultValue) {
        super(parent, key, defaultValue, Codec.STRING);
        super.set(new HashMap<>(defaultValue));
    }
}
