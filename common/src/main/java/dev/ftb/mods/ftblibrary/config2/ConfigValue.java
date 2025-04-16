package dev.ftb.mods.ftblibrary.config2;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class ConfigValue<T> implements Supplier<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigValue.class);

    private FTBConfig parent;
    private final String key;
    private String[] comment;

    private T value;
    private final T defaultValue;
    private final Codec<T> codec;

    public ConfigValue(FTBConfig parent, String key, T defaultValue, Codec<T> codec) {
        this.parent = parent;
        this.key = key;
        this.defaultValue = defaultValue;
        this.codec = codec;
    }

    @Override
    public T get() {
        if (value == null) {
            value = defaultValue;
        }

        return value;
    }

    public ConfigValue<T> comment(String... comment) {
        this.comment = comment;
        return this;
    }

    public String key() {
        return key;
    }

    public String[] comment() {
        return comment;
    }

    public void set(T value) {
        this.value = value;
    }

    public FTBConfig parent() {
        return parent;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public T read(Tag tag) {
        return codec.parse(NbtOps.INSTANCE, tag)
                .resultOrPartial((string2) -> LOGGER.error("Failed to read field {}: {}", tag, string2))
                .orElse(defaultValue);
    }

    void readFromTag(Tag tag) {
        this.set(this.read(tag));
    }

    public Tag write() {
        return codec.encodeStart(NbtOps.INSTANCE, this.get()).getOrThrow();
    }
}
