package dev.ftb.mods.ftblibrary.client.config.editable;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.gui.EditConfigListScreen;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public class EditableList<E, CV extends EditableConfigValue<E>> extends EditableConfigValue<List<E>> {
    public static final Component EMPTY_LIST = Component.literal("[]");
    public static final Component NON_EMPTY_LIST = Component.literal("[...]");

    public static final Color4I COLOR = Color4I.rgb(0xFFAA49);

    private final CV type;

    public EditableList(CV type) {
        this.type = type;
    }

    public CV getType() {
        return type;
    }

    @Override
    public List<E> copy(List<E> v) {
        List<E> list = new ArrayList<>(v.size());

        for (var value : v) {
            list.add(type.copy(value));
        }

        return list;
    }

    @Override
    public Color4I getColor(List<E> value, Theme theme) {
        return theme.hasDarkBackground() ? EditableString.COLOR_HI : EditableString.COLOR_LO;
    }

    @Override
    public void addInfo(TooltipList l, Theme theme) {
        if (!value.isEmpty()) {
            l.add(info("List"));

            ChatFormatting col = theme.hasDarkBackground() ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY;
            for (int i = 0; i < value.size(); i++) {
                if (i >= 10) {
                    // prevent big lists producing giant unwieldy tooltips
                    l.add(Component.literal("... " + (value.size() - i) + " more ...").withStyle(col, ChatFormatting.ITALIC));
                    break;
                }
                var element = value.get(i);
                l.add(type.getStringForGUI(element));
            }

            if (!defaultValue.isEmpty()) {
                l.blankLine();
            }
        }

        if (!defaultValue.isEmpty()) {
            l.add(info("Default"));
            for (var value : defaultValue) {
                l.add(type.getStringForGUI(value));
            }
        }
    }

    @Override
    public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
        new EditConfigListScreen<>(this, callback).openGui();
    }

    @Override
    public Component getStringForGUI(List<E> value) {
        return value.isEmpty() ? EMPTY_LIST : formatListSize(value);
    }

    private Component formatListSize(List<E> v) {
        MutableComponent main = v.size() == 1 ?
                Component.translatable("ftblibrary.gui.listSize1") :
                Component.translatable("ftblibrary.gui.listSize", v.size());
        return Component.literal("[ ").append(main.withStyle(ChatFormatting.ITALIC)).append(" ]");
    }
}
