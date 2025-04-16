package dev.ftb.mods.ftblibrary.config2;

import com.mojang.serialization.Codec;

import java.util.List;
import java.util.Map;

public abstract class AbstractValueCreator {
    public ConfigValue<String> stringValue(String key, String defaultValue) {
        return this.addValue(key, defaultValue, Codec.STRING);
    }

    public ConfigValue<Boolean> boolValue(String key, boolean defaultValue) {
        return this.addValue(key, defaultValue, Codec.BOOL);
    }

    public ConfigValue<Integer> integerValue(String key, int defaultValue) {
        return this.addValue(key, defaultValue, Codec.INT);
    }

    public ConfigValue<Double> doubleValue(String key, double defaultValue) {
        return this.addValue(key, defaultValue, Codec.DOUBLE);
    }

    public ConfigValue<Float> floatValue(String key, float defaultValue) {
        return this.addValue(key, defaultValue, Codec.FLOAT);
    }

    public ConfigValue<Long> longValue(String key, long defaultValue) {
        return this.addValue(key, defaultValue, Codec.LONG);
    }

    public ConfigValue<Byte> byteValue(String key, byte defaultValue) {
        return this.addValue(key, defaultValue, Codec.BYTE);
    }

    public ConfigValue<Short> shortValue(String key, short defaultValue) {
        return this.addValue(key, defaultValue, Codec.SHORT);
    }

    public ConfigValue<List<String>> stringListValue(String key, List<String> defaultValue) {
        return this.addValue(key, defaultValue, Codec.list(Codec.STRING));
    }

    public ConfigValue<List<Integer>> integerListValue(String key, List<Integer> defaultValue) {
        return this.addValue(key, defaultValue, Codec.list(Codec.INT));
    }

    public <T> ConfigValue<T> codecValue(String key, T defaultValue, Codec<T> codec) {
        return this.addValue(key, defaultValue, codec);
    }

    public <K, V> ConfigValue<Map<K, V>> mapValue(String key, Map<K, V> defaultValue, Codec<Map<K, V>> codec) {
        return this.addValue(key, defaultValue, codec);
    }

    abstract <T> ConfigValue<T> addValue(String key, T defaultValue, Codec<T> codec);
}
