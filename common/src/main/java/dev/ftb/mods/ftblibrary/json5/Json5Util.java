package dev.ftb.mods.ftblibrary.json5;

import com.mojang.serialization.Codec;
import de.marhali.json5.*;
import de.marhali.json5.exception.Json5Exception;
import net.minecraft.core.HolderLookup;
import org.apache.commons.io.Charsets;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class Json5Util {
    public static final String FILE_EXT = ".json5";

    public static Optional<String> getString(Json5Object json, String field) {
        return getSomething(json, field, Json5Primitive::isString, Json5Primitive::getAsString);
    }

    public static Optional<Integer> getInt(Json5Object json, String field) {
        return getSomething(json, field, Json5Primitive::isNumber, Json5Primitive::getAsInt);
    }

    public static Optional<Long> getLong(Json5Object json, String field) {
        return getSomething(json, field, Json5Primitive::isNumber, Json5Primitive::getAsLong);
    }

    public static Optional<Float> getFloat(Json5Object json, String field) {
        return getSomething(json, field, Json5Primitive::isNumber, Json5Primitive::getAsFloat);
    }

    public static Optional<Double> getDouble(Json5Object json, String field) {
        return getSomething(json, field, Json5Primitive::isNumber, Json5Primitive::getAsDouble);
    }

    public static Optional<Boolean> getBoolean(Json5Object json, String field) {
        return getSomething(json, field, Json5Primitive::isBoolean, Json5Primitive::getAsBoolean);
    }

    private static <T> Optional<T> getSomething(Json5Object json, String field, Predicate<Json5Primitive> pred, Function<Json5Primitive,T> mapper) {
        return json.get(field) instanceof Json5Primitive p && pred.test(p) ? Optional.of(mapper.apply(p)) : Optional.empty();
    }

    public static Optional<Json5Object> getJson5Object(Json5Object json, String field) {
        return json.get(field) instanceof Json5Object o ? Optional.of(o) : Optional.empty();
    }

    public static Optional<Json5Array> getJson5Array(Json5Object json, String field) {
        return json.get(field) instanceof Json5Array a ? Optional.of(a) : Optional.empty();
    }

    public static <T> void store(Json5Object json, String field, Codec<T> codec, T object) {
        json.add(field, codec.encodeStart(Json5Ops.INSTANCE, object).getOrThrow());
    }

    public static <T> void store(Json5Object json, HolderLookup.Provider lookup, String field, Codec<T> codec, T object) {
        json.add(field, codec.encodeStart(lookup.createSerializationContext(Json5Ops.INSTANCE), object).getOrThrow());
    }

    public static <T> Optional<T> fetch(Json5Object json, String field, Codec<T> codec) {
        return codec.parse(Json5Ops.INSTANCE, json.get(field)).result();
    }

    public static <T> Optional<T> fetch(Json5Object json, HolderLookup.Provider lookup, String field, Codec<T> codec) {
        return codec.parse(lookup.createSerializationContext(Json5Ops.INSTANCE), json.get(field)).result();
    }

    public static Json5Object load(Path inputFile) throws IOException {
        return load(inputFile, Json5Object.class);
    }

    public static <T extends Json5Element> T load(Path inputFile, Class<T> jsonCls) throws IOException {
        try (FileInputStream stream = new FileInputStream(inputFile.toFile())) {
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            try {
                var json = new Json5().parse(reader);
                if (jsonCls.isAssignableFrom(json.getClass())) {
                    return jsonCls.cast(json);
                } else {
                    throw new IOException("expected object of type " + jsonCls.getName());
                }
            } catch (Json5Exception ex) {
                throw new IOException("caught Json5Exception while parsing " + inputFile + ": " + ex.getMessage());
            }
        }
    }

    public static void save(Path path, Json5Element json) throws IOException {
        Path parent = path.getParent();
        Files.createDirectories(parent);
        Path tmp = parent.resolve(path.getFileName().toString() + ".tmp");
        Files.writeString(tmp, new Json5().serialize(json), StandardCharsets.UTF_8);
        try {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /// Use [#load(Path)]
    @Deprecated(forRemoval = true, since = "26.1.2.2")
    public static Json5Object tryRead(Path inputFile) throws IOException {
        return load(inputFile, Json5Object.class);
    }

    /// Use [#load(Path, Class)]
    @Deprecated(forRemoval = true, since = "26.1.2.2")
    public static <T extends Json5Element> T tryRead(Path inputFile, Class<T> jsonCls) throws IOException {
        return load(inputFile, jsonCls);
    }

    /// Use [#save(Path, Json5Element)]
    @Deprecated(forRemoval = true, since = "26.1.2.1")
    public static void tryWrite(Path outputFile, Json5Object json) throws IOException {
        save(outputFile, json);
    }

    /// Use [#save(Path, Json5Element)]
    @Deprecated(forRemoval = true, since = "26.1.2.1")
    public static void tryWrite(Path outputFile, Json5Element json) throws IOException {
        save(outputFile, json);
    }
}
