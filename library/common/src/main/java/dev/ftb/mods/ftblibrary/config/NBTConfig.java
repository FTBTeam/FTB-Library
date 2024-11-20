package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class NBTConfig extends ConfigFromString<CompoundTag> {
    public static final Component EMPTY_NBT = Component.literal("{}");
    public static final Component NON_EMPTY_NBT = Component.literal("{...}");

    @Override
    public CompoundTag copy(CompoundTag v) {
        return v.copy();
    }

    @Override
    public String getStringFromValue(@Nullable CompoundTag v) {
        return v == null ? "null" : v.toString();
    }

    @Override
    public Component getStringForGUI(@Nullable CompoundTag v) {
        return v == null ? NULL_TEXT : v.isEmpty() ? EMPTY_NBT : NON_EMPTY_NBT;
    }

    @Override
    public boolean parse(@Nullable Consumer<CompoundTag> callback, String string) {
        if (string.equals("null")) {
            return okValue(callback, null);
        }

        try {
            return okValue(callback, TagParser.parseTag(string));
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void addInfo(TooltipList list) {
        list.add(info("Value", value == null ? "null" : value));
        list.add(info("Default", defaultValue == null ? "null" : defaultValue));
    }
}
