package dev.ftb.mods.ftblibrary.util;

import de.marhali.json5.Json5;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import de.marhali.json5.Json5Primitive;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class Json5Util {
    public static Optional<String> getString(Json5Object json, String field) {
        return getSomethingOr(json, field, Json5Primitive::isString, Json5Primitive::getAsString);
    }

    public static Optional<Integer> getInt(Json5Object json, String field) {
        return getSomethingOr(json, field, Json5Primitive::isNumber, Json5Primitive::getAsInt);
    }

    public static Optional<Boolean> getBoolean(Json5Object json, String field) {
        return getSomethingOr(json, field, Json5Primitive::isBoolean, Json5Primitive::getAsBoolean);
    }

    private static <T> Optional<T> getSomethingOr(Json5Object json, String field, Predicate<Json5Primitive> pred, Function<Json5Primitive,T> mapper) {
        return json.get(field) instanceof Json5Primitive p && pred.test(p) ? Optional.of(mapper.apply(p)) : Optional.empty();
    }

    public static Optional<Json5Object> getJson5Object(Json5Object json, String field) {
        Json5Element el = json.get(field);
        return el != null && el.isJson5Object() ? Optional.of(el.getAsJson5Object()) : Optional.empty();
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
