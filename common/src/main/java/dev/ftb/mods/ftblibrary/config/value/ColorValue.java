package dev.ftb.mods.ftblibrary.config.value;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableConfigValue;
import dev.ftb.mods.ftblibrary.config.serializer.ConfigSerializer;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import org.apache.commons.lang3.Validate;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class ColorValue extends BaseValue<Color4I> {
    public static final String DEF_COLOR = "FFFFFFFF";

    protected ColorValue(Config config, String name, Color4I def) {
        super(config, name, def);
    }

    @Override
    public void write(ConfigSerializer serializer) {
        StringValue val = new StringValue(Objects.requireNonNull(parent), key, DEF_COLOR);
        val.set(String.format("%08x", get().rgba()));
        serializer.putString(key, val);
    }

    @Override
    public void read(ConfigSerializer serializer) {
        String str = serializer.getString(key, DEF_COLOR);
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
    protected @Nullable EditableConfigValue<?> fillClientConfig(EditableConfigGroup group) {
        return group.addColor(key, get(), this::set, defaultValue).withAlphaEditing();
    }
}
