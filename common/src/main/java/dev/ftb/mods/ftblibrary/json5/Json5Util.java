package dev.ftb.mods.ftblibrary.json5;

import com.mojang.serialization.Codec;
import de.marhali.json5.Json5;
import de.marhali.json5.Json5Array;
import de.marhali.json5.Json5Object;
import de.marhali.json5.Json5Primitive;
import net.minecraft.core.HolderLookup;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static Json5Object tryRead(Path inputFile) throws IOException {
        try (FileInputStream stream = new FileInputStream(inputFile.toFile())) {
            var json = new Json5().parse(stream);
            if (json instanceof Json5Object o) {
                return o;
            } else {
                throw new IOException("expected Json5 object");
            }
        }
    }

    public static void tryWrite(Path outputFile, Json5Object json) throws IOException {
        Files.writeString(outputFile, new Json5().serialize(json));
    }
}
