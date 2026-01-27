package dev.ftb.mods.ftblibrary.client.config.editable;

import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public class EditableString extends EditableStringifiedConfig<String> {
    public static final Color4I COLOR_HI = Color4I.rgb(0xFFAA49);
    public static final Color4I COLOR_LO = Color4I.rgb(0x805524);

    @Nullable
    public final Pattern pattern;

    public EditableString(@Nullable Pattern pattern) {
        this.pattern = pattern;

        defaultValue = "";
        value = "";
    }

    public EditableString() {
        this(null);
    }

    @Override
    public Color4I getColor(String value, Theme theme) {
        return theme.hasDarkBackground() ? COLOR_HI : COLOR_LO;
    }

    @Override
    public boolean parse(@Nullable Consumer<String> callback, String string) {
        return (pattern == null || pattern.matcher(string).matches()) && okValue(callback, string);
    }

    @Override
    public Component getStringForGUI(String value) {
        return Component.literal('"' + value + '"');
    }

    @Override
    public void addInfo(TooltipList list, Theme theme) {
        if (!value.equals(defaultValue)) {
            list.add(Component.translatable("config.group.value").append(": ").withStyle(ChatFormatting.AQUA)
                    .append(Component.literal("\"" + value + "\"").withStyle(ChatFormatting.WHITE)));
        }

        super.addInfo(list, theme);

        if (pattern != null) {
            list.add(info("Regex", pattern.pattern()));
        }
    }

    @Override
    public int getExtraEditorWidth() {
        return 200;
    }
}
