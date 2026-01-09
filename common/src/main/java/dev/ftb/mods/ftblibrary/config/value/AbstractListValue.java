package dev.ftb.mods.ftblibrary.config.value;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftblibrary.config.serializer.ConfigSerializer;
import net.minecraft.nbt.StringTag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractListValue<T> extends BaseValue<List<T>> {
    private final Codec<T> codec;

    protected AbstractListValue(ConfigGroup parent, String key, List<T> defaultValue, Codec<T> codec) {
        super(parent, key, defaultValue);
        this.codec = codec;
        super.set(new ArrayList<>(defaultValue));
    }

    @Override
    public void set(List<T> value) {
        super.set(new ArrayList<>(value));
    }

    @Override
    public void write(ConfigSerializer serializer) {
        serializer.putList(key, this, codec);
    }

    @Override
    protected void addExtraHeaderInfo(List<String> header) {
        // override this if the list value type doesn't have a toString() method which creates nice human-readable output
        header.add("Default: [" + defaultValue.stream()
                .map(t -> StringTag.quoteAndEscape(t.toString()))
                .collect(Collectors.joining(", "))
                + "]"
        );
    }

    @Override
    public void read(ConfigSerializer serializer) {
        set(serializer.getList(key, defaultValue, codec));
    }
}
