package dev.ftb.mods.ftblibrary.config.serializer;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftblibrary.config.value.*;
import dev.ftb.mods.ftblibrary.util.NameMap;

import java.util.List;
import java.util.Map;

/**
 * An abstraction of the various get/put methods for serialization of config value types.
 */
public interface ConfigSerializer {
    List<BaseValue<?>> getConfigSection(String key, List<BaseValue<?>> defVal, boolean topLevel);
    void putConfigSection(String key, ConfigGroup val, boolean topLevel);

    boolean getBoolean(String key, boolean defVal);
    void putBoolean(String key, BooleanValue val);

    double getDouble(String key, double defVal);
    void putDouble(String key, DoubleValue val);

    int getInt(String key, int defVal);
    void putInt(String key, IntValue val);

    long getLong(String key, long defVal);
    void putLong(String key, LongValue val);

    String getString(String key, String defVal);
    void putString(String key, StringValue val);

    <T> List<T> getList(String key, List<T> defVal, Codec<T> codec);
    <T> void putList(String key, AbstractListValue<T> val, Codec<T> codec);

    <T> Map<String,T> getMap(String key, Map<String,T> defVal, Codec<T> codec);
    <T> void putMap(String key, AbstractMapValue<T> defVal, Codec<T> codec);

    int[] getIntArray(String key, int[] defVal);
    void putIntArray(String key, IntArrayValue val);

    <E> E getEnum(String key, E defVal, NameMap<E> nameMap);
    <E> void putEnum(String key, EnumValue<E> val, NameMap<E> nameMap);
}
