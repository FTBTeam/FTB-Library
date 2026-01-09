package dev.ftb.mods.ftblibrary.config.serializer;

import com.mojang.serialization.Codec;
import de.marhali.json5.*;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.value.*;
import dev.ftb.mods.ftblibrary.util.Json5Ops;
import dev.ftb.mods.ftblibrary.util.NameMap;
import net.minecraft.util.Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

public record Json5ConfigSerializer(Json5Object configJson) implements ConfigSerializer {
    public static Json5Object serialize(Config config) {
        Json5ConfigSerializer serializer = new Json5ConfigSerializer(new Json5Object());
        config.write(serializer);
        return serializer.configJson;
    }

    public static void readFromFile(Config config, Path path) throws IOException {
        if (!Files.exists(path)) {
            FTBLibrary.LOGGER.info("creating new default config file at {}", path);
            writeToFile(config, path);
        }
        try (FileInputStream input = new FileInputStream(path.toFile())) {
            var element = new Json5().parse(input);
            config.read(new Json5ConfigSerializer(element.getAsJson5Object()));
        } catch (FileNotFoundException | IllegalStateException e) {
            throw new IOException(e);
        }
    }

    public static void writeToFile(Config config, Path path) throws IOException {
        Files.writeString(path, new Json5().serialize(serialize(config)));
    }

    @Override
    public List<BaseValue<?>> getConfigSection(String key, List<BaseValue<?>> defVal, boolean topLevel) {
        if (topLevel) {
            return readFromJson(configJson, defVal);
        } else if (configJson.has(key)) {
            return readFromJson(configJson.getAsJson5Object(key), defVal);
        } else {
            return List.of();
        }
    }

    private List<BaseValue<?>> readFromJson(Json5Object json, List<BaseValue<?>> defVal) {
        Json5ConfigSerializer serializer = new Json5ConfigSerializer(json);

        List<BaseValue<?>> res = new ArrayList<>();
        for (var value : defVal) {
            if (json.has(value.key)) {
                value.read(serializer);
                res.add(value);
            }
        }
        return res;
    }

    @Override
    public void putConfigSection(String key, Config val, boolean topLevel) {
        if (topLevel) {
            configJson.setComment(val.getCommentString());
            writeToJson(val.get(), configJson);
        } else {
            configJson.add(key, writeToJson(val.get(), Util.make(new Json5Object(), json -> json.setComment(val.getCommentString()))));
        }
    }

    private Json5Object writeToJson(List<BaseValue<?>> values, Json5Object json) {
        Json5ConfigSerializer serializer = new Json5ConfigSerializer(json);

        values.stream().sorted().toList().forEach(value -> value.write(serializer));

        return json;
    }

    @Override
    public boolean getBoolean(String key, boolean defVal) {
        return getPrimitive(configJson, key, Json5Primitive::getAsBoolean, defVal);
    }

    @Override
    public void putBoolean(String key, BooleanValue val) {
        configJson.add(key, makeCommentedPrimitive(val, Json5Primitive::fromBoolean));
    }

    @Override
    public double getDouble(String key, double defVal) {
        return getPrimitive(configJson, key, Json5Primitive::getAsDouble, defVal);
    }

    @Override
    public void putDouble(String key, DoubleValue val) {
        configJson.add(key, makeCommentedPrimitive(val, Json5Primitive::fromNumber));
    }

    @Override
    public int getInt(String key, int defVal) {
        return getPrimitive(configJson, key, Json5Primitive::getAsInt, defVal);
    }

    @Override
    public void putInt(String key, IntValue val) {
        configJson.add(key, makeCommentedPrimitive(val, Json5Primitive::fromNumber));
    }

    @Override
    public long getLong(String key, long defVal) {
        return getPrimitive(configJson, key, Json5Primitive::getAsLong, defVal);
    }

    @Override
    public void putLong(String key, LongValue val) {
        configJson.add(key, makeCommentedPrimitive(val, Json5Primitive::fromNumber));
    }

    @Override
    public <T> List<T> getList(String key, List<T> defVal, Codec<T> codec) {
        if (configJson.get(key) instanceof Json5Array array) {
            return Util.make(new ArrayList<>(),
                    list -> array.asList().forEach(el -> codec.parse(Json5Ops.INSTANCE, el).ifSuccess(list::add))
            );
        }
        return defVal;
    }

    @Override
    public <T> void putList(String key, AbstractListValue<T> val, Codec<T> codec) {
        configJson.add(key, Util.make(new Json5Array(val.get().size()), a -> {
                    a.setComment(val.getCommentString());
                    val.get().forEach(el -> codec.encodeStart(Json5Ops.INSTANCE, el).ifSuccess(a::add));
                })
        );
    }

    @Override
    public <T> Map<String, T> getMap(String key, Map<String, T> defVal, Codec<T> codec) {
        try {
            Map<String,T> res = new HashMap<>();
            configJson.getAsJson5Object(key).asMap().forEach((k, v) ->
                    codec.parse(Json5Ops.INSTANCE, v).ifSuccess(s -> res.put(k, s))
            );
            return res;
        } catch (ClassCastException e) {
            return defVal;
        }
    }

    @Override
    public <T> void putMap(String key, AbstractMapValue<T> val, Codec<T> codec) {
        configJson.add(key, Util.make(new Json5Object(), o -> val.get().forEach((k, v) -> {
                    o.setComment(val.getCommentString());
                    codec.encodeStart(Json5Ops.INSTANCE, v).ifSuccess(el -> o.add(k, el));
                }))
        );
    }

    @Override
    public String getString(String key, String defVal) {
        return getPrimitive(configJson, key, Json5Primitive::getAsString, defVal);
    }

    @Override
    public void putString(String key, StringValue val) {
        configJson.add(key, makeCommentedPrimitive(val, Json5Primitive::fromString));
    }

    @Override
    public int[] getIntArray(String key, int[] defVal) {
        try {
            return configJson.getAsJson5Array(key).asList().stream().map(Json5Element::getAsInt).mapToInt(i -> i).toArray();
        } catch (Exception ignored) {
        }
        return defVal;
    }

    @Override
    public void putIntArray(String key, IntArrayValue val) {
        configJson.add(key, Util.make(new Json5Array(val.get().length), a -> {
            a.setComment(val.getCommentString());
            Arrays.stream(val.get()).forEach(a::add);
        }));
    }

    @Override
    public <E> E getEnum(String key, E defVal, NameMap<E> nameMap) {
        try {
            return nameMap.get(configJson.getAsJson5Primitive(key).getAsString());
        } catch (ClassCastException ignored) {
            return defVal;
        }
    }

    @Override
    public <E> void putEnum(String key, EnumValue<E> val, NameMap<E> nameMap) {
        Json5Primitive strProp = Json5Primitive.fromString(nameMap.getName(val.get()));
        strProp.setComment(val.getCommentString());
        configJson.add(key, strProp);
    }

    private static <T> T getPrimitive(Json5Object json5Object, String key, Function<Json5Primitive,T> func, T defVal) {
        if (json5Object.get(key) instanceof Json5Primitive primitive) {
            try {
                return func.apply(primitive);
            } catch (ClassCastException | NumberFormatException ignored) {
            }
        }
        return defVal;
    }

    private static <T> Json5Primitive makeCommentedPrimitive(BaseValue<T> val, Function<T, Json5Primitive> func) {
        return Util.make(func.apply(val.get()), p -> p.setComment(val.getCommentString()));
    }

}
