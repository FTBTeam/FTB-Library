package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

public class ColorValue extends BaseValue<Color4I> {
    protected ColorValue(@Nullable SNBTConfig c, String n, Color4I def) {
        super(c, n, def);
    }

    @Override
    public void write(SNBTCompoundTag tag) {
        tag.comment(key, String.format("Default: %08x", defaultValue.rgba()));
        tag.putString(key, String.format("%08x", get().rgba()));
    }

    @Override
    public void read(SNBTCompoundTag tag) {
        String str = tag.getString(key);
        try {
            // can't just use Integer.parseInt() here - it chokes on hex values which represent a negative int
            Validate.isTrue(str.length() == 8);
            int a = Integer.parseInt(str.substring(0, 2), 16);
            int r = Integer.parseInt(str.substring(2, 4), 16);
            int g = Integer.parseInt(str.substring(4, 6), 16);
            int b = Integer.parseInt(str.substring(2, 8), 16);
            set(Color4I.rgba(r, g, b, a));
        } catch (IllegalArgumentException e) {
            FTBLibrary.LOGGER.error("can't parse hex argb color '{}'", str);
            set(defaultValue);
        }
    }

    @Override
    public void createClientConfig(ConfigGroup group) {
        group.addColor(key, get(), this::set, defaultValue).withAlphaEditing();
    }
}
