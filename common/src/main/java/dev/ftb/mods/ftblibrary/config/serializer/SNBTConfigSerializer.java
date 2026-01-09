package dev.ftb.mods.ftblibrary.config.serializer;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.value.*;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.util.NameMap;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public record SNBTConfigSerializer(SNBTCompoundTag configTag) implements ConfigSerializer {
    public static SNBTCompoundTag serialize(ConfigGroup config) {
        SNBTConfigSerializer serializer = new SNBTConfigSerializer(new SNBTCompoundTag());
        config.write(serializer);
        return serializer.configTag;
    }

    public static void readFromFile(ConfigGroup config, Path path) throws IOException {
        if (!Files.exists(path)) {
            FTBLibrary.LOGGER.info("creating new default config file at {}", path);
            writeToFile(config, path);
        }
        config.read(new SNBTConfigSerializer(SNBT.tryRead(path)));
    }

    public static void writeToFile(ConfigGroup config, Path path) throws IOException {
        SNBT.tryWrite(path, serialize(config));
    }

    @Override
    public List<BaseValue<?>> getConfigSection(String key, List<BaseValue<?>> defVal, boolean topLevel) {
        if (topLevel) {
            return readFromTag(configTag, defVal);
        } else if (configTag.contains(key)) {
            return readFromTag(configTag.getAsSnbtComponent(key), defVal);
        } else {
            return List.of();
        }
    }

    private List<BaseValue<?>> readFromTag(SNBTCompoundTag tag, List<BaseValue<?>> defVal) {
        SNBTConfigSerializer serializer = new SNBTConfigSerializer(tag);
        List<BaseValue<?>> res = new ArrayList<>();
        for (var value : defVal) {
            if (tag.contains(value.key)) {
                value.read(serializer);
                res.add(value);
            }
        }
        return res;
    }

    @Override
    public void putConfigSection(String key, ConfigGroup val, boolean topLevel) {
        if (topLevel) {
            configTag.comment("", val.getCommentString());
            writeToTag(val.get(), configTag);
        } else {
            configTag.comment(key, val.getCommentString());
            configTag.put(key, writeToTag(val.get(), new SNBTCompoundTag()));
        }
    }

    private SNBTCompoundTag writeToTag(List<BaseValue<?>> values, SNBTCompoundTag tag) {
        SNBTConfigSerializer serializer = new SNBTConfigSerializer(tag);
        values.stream().sorted().toList().forEach(value -> {
            tag.comment(value.key, value.getCommentString());
            value.write(serializer);
        });
        return tag;
    }

    @Override
    public boolean getBoolean(String key, boolean defVal) {
        return configTag.getBooleanOr(key, defVal);
    }

    @Override
    public void putBoolean(String key, BooleanValue val) {
        configTag.putBoolean(key, val.get());
    }

    @Override
    public double getDouble(String key, double defVal) {
        return configTag.getDoubleOr(key, defVal);
    }

    @Override
    public void putDouble(String key, DoubleValue val) {
        configTag.putDouble(key, val.get());
    }

    @Override
    public int getInt(String key, int defVal) {
        return configTag.getIntOr(key, defVal);
    }

    @Override
    public void putInt(String key, IntValue val) {
        configTag.putInt(key, val.get());
    }

    @Override
    public long getLong(String key, long defVal) {
        return configTag.getLongOr(key, defVal);
    }

    @Override
    public void putLong(String key, LongValue val) {
        configTag.putLong(key, val.get());
    }

    @Override
    public <T> void putList(String key, AbstractListValue<T> val, Codec<T> codec) {
        configTag.put(key, Util.make(new ListTag(), l ->
                val.get().forEach(el -> codec.encodeStart(NbtOps.INSTANCE, el).ifSuccess(l::add)))
        );
    }

    @Override
    public <T> List<T> getList(String key, List<T> defVal, Codec<T> codec) {
        if (configTag.get(key) instanceof ListTag list) {
            return Util.make(new ArrayList<>(), a ->
                    list.forEach(tag -> codec.parse(NbtOps.INSTANCE, tag).ifSuccess(a::add))
            );
        }
        return List.of();
    }

    @Override
    public <T> Map<String, T> getMap(String key, Map<String, T> defVal, Codec<T> codec) {
        Map<String,T> res = new HashMap<>();
        try {
            configTag.getCompound(key).orElseThrow().forEach((k, v) ->
                    codec.parse(NbtOps.INSTANCE, v).ifSuccess(s -> res.put(k, s))
            );
            return res;
        } catch (NoSuchElementException ignored) {
            return defVal;
        }
    }

    @Override
    public <T> void putMap(String key, AbstractMapValue<T> val, Codec<T> codec) {
        configTag.put(key, Util.make(new SNBTCompoundTag(), t -> val.get().forEach((k, v) ->
                codec.encodeStart(NbtOps.INSTANCE, v).ifSuccess(tag -> t.put(k, tag))))
        );
    }

    @Override
    public String getString(String key, String defVal) {
        return configTag.getStringOr(key, defVal);
    }

    @Override
    public void putString(String key, StringValue val) {
        configTag.putString(key, val.get());
    }

    @Override
    public int[] getIntArray(String key, int[] defVal) {
        return configTag.getIntArray(key).orElse(defVal);
    }

    @Override
    public void putIntArray(String key, IntArrayValue val) {
        configTag.putIntArray(key, val.get());
    }

    @Override
    public <E> E getEnum(String key, E defVal, NameMap<E> nameMap) {
        return configTag.getString(key).map(nameMap::get).orElse(defVal);
    }

    @Override
    public <E> void putEnum(String key, EnumValue<E> val, NameMap<E> nameMap) {
        configTag.putString(key, nameMap.getName(val.get()));
    }
}
