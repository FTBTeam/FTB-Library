package dev.ftb.mods.ftblibrary.snbt;

import net.minecraft.nbt.*;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class SNBT {
    private static boolean shouldSortKeysOnWrite = false;

    /**
     * Parse an SNBT compound from string data
     *
     * @param lines the lines of serialized SNBT data
     * @return a
     */
    public static SNBTCompoundTag readLines(List<String> lines) {
        return SNBTParser.read(lines);
    }

    public static SNBTCompoundTag tryRead(Path path) throws IOException {
        return readLines(Files.readAllLines(path, StandardCharsets.UTF_8));
    }

    public static void tryWrite(Path path, CompoundTag tag) throws IOException {
        if (Files.notExists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        Files.write(path, writeLines(tag));
    }

    public static List<String> writeLines(CompoundTag nbt) {
        var builder = new SNBTBuilder();

        var rootProperties = nbt instanceof SNBTCompoundTag ? ((SNBTCompoundTag) nbt).getProperties("") : SNBTTagProperties.DEFAULT;

        if (!rootProperties.comment.isEmpty()) {
            for (var s : rootProperties.comment.split("\n")) {
                builder.print("# ");
                builder.print(s);
                builder.println();
            }

            builder.println();
        }

        append(builder, nbt);
        builder.println();
        return builder.lines;
    }

    /**
     * Don't use this anymore
     * @param path path to write to
     * @param nbt nbt compound to write
     * @return true if config was written
     * @deprecated use {@link #tryWrite(Path, CompoundTag)}
     */
    @Deprecated
    public static boolean write(Path path, CompoundTag nbt) {
        try {
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            Files.write(path, writeLines(nbt));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static void append(SNBTBuilder builder, @Nullable Tag nbt) {
        switch (nbt) {
            case null -> builder.print("null");
            case EndTag ignoredEndTag -> builder.print("null");
            case CompoundTag compound -> appendCompound(builder, compound);
            case CollectionTag collectionTag -> {
                switch (collectionTag) {
                    case ByteArrayTag ignored -> appendCollection(builder, collectionTag, "B;");
                    case IntArrayTag ignored -> appendCollection(builder, collectionTag, "I;");
                    case LongArrayTag ignored -> appendCollection(builder, collectionTag, "L;");
                    default -> appendCollection(builder, collectionTag,"");
                }
            }
            case StringTag stringTag -> builder.print(SNBTUtils.quoteAndEscape(stringTag.asString().orElse("")));
            default -> builder.print(nbt.toString());
        }
    }

    private static void appendCompound(SNBTBuilder builder, CompoundTag compound) {
        var snbtCompoundTag = compound instanceof SNBTCompoundTag s ? s : null;

        if (compound.isEmpty()) {
            builder.print("{ }");
            return;
        }

        if (snbtCompoundTag != null && snbtCompoundTag.singleLine) {
            builder.singleLine++;
        }

        var singleLine = builder.singleLine > 0;

        builder.print("{");

        if (singleLine) {
            builder.print(" ");
        } else {
            builder.println();
            builder.push();
        }

        var index = 0;

        Collection<String> keys = shouldSortKeysOnWrite ? compound.keySet().stream().sorted().toList() : compound.keySet();
        for (var key : keys) {
            index++;
            var properties = snbtCompoundTag == null ? SNBTTagProperties.DEFAULT : snbtCompoundTag.getProperties(key);

            if (!properties.comment.isEmpty()) {
                if (singleLine) {
                    throw new IllegalStateException("Can't have singleLine enabled and a comment at the same time!");
                }

                if (index != 1) {
                    builder.println();
                }

                for (var s : properties.comment.split("\n")) {
                    builder.print("# ");
                    builder.print(s);
                    builder.println();
                }
            }

            builder.print(key.isEmpty() ? "\"\"" : SNBTUtils.handleEscape(key));
            builder.print(": ");

            if (properties.valueType == SNBTTagProperties.TYPE_FALSE) {
                builder.print("false");
            } else if (properties.valueType == SNBTTagProperties.TYPE_TRUE) {
                builder.print("true");
            } else {
                if (properties.singleLine) {
                    builder.singleLine++;
                }

                append(builder, compound.get(key));

                if (properties.singleLine) {
                    builder.singleLine--;
                }
            }

            if (singleLine && index != compound.size()) {
                builder.print(",");
            }

            if (singleLine) {
                builder.print(" ");
            } else {
                if (index < compound.size()) {
                    builder.print(",");
                }
                builder.println();
            }
        }

        if (!singleLine) {
            builder.pop();
        }

        builder.print("}");

        if (snbtCompoundTag != null && snbtCompoundTag.singleLine) {
            builder.singleLine--;
        }
    }

    private static void appendCollection(SNBTBuilder builder, CollectionTag nbt, String opening) {
        if (nbt.isEmpty()) {
            builder.print("[");
            builder.print(opening);
            builder.print(" ]");
            return;
        } else if (nbt.size() == 1) {
            builder.print("[");
            builder.print(opening);
            append(builder, nbt.get(0));
            builder.print("]");
            return;
        }

        var singleLine = builder.singleLine > 0;

        builder.print("[");
        builder.print(opening);

        if (singleLine) {
            builder.print(" ");
        } else {
            builder.println();
            builder.push();
        }

        var index = 0;

        for (Tag value : nbt) {
            index++;
            append(builder, value);

            if (singleLine && index != nbt.size()) {
                builder.print(",");
            }

            if (singleLine) {
                builder.print(" ");
            } else {
                if (index < nbt.size()) {
                    builder.print(",");
                }
                builder.println();
            }
        }

        if (!singleLine) {
            builder.pop();
        }

        builder.print("]");
    }

    public static boolean shouldSortKeysOnWrite() {
        return shouldSortKeysOnWrite;
    }

    public static boolean setShouldSortKeysOnWrite(boolean shouldSortKeysOnWrite) {
        boolean prev = SNBT.shouldSortKeysOnWrite;
        SNBT.shouldSortKeysOnWrite = shouldSortKeysOnWrite;
        return prev;
    }
}
