package dev.ftb.mods.ftblibrary.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

public class TooltipList {
    private final List<Component> lines = new ArrayList<>();
    public int backgroundColor = 0xC0100010;
    public int borderColorStart = 0x505000FF;
    public int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
    public int maxWidth = 0;
    public int xOffset = 0;
    public int yOffset = 0;

    public boolean shouldRender() {
        return !lines.isEmpty();
    }

    public void reset() {
        lines.clear();
        backgroundColor = 0xC0100010;
        borderColorStart = 0x505000FF;
        borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
        maxWidth = 0;
        xOffset = 0;
        yOffset = 0;
    }

    public void add(Component component) {
        lines.add(component);
    }

    public void blankLine() {
        add(Component.empty());
    }

    public void styledString(String text, Style style) {
        add(Component.literal(text).withStyle(style));
    }

    public void styledString(String text, ChatFormatting color) {
        add(Component.literal(text).withStyle(color));
    }

    public void styledTranslate(String key, Style style, Object... objects) {
        add(Component.translatable(key, objects).withStyle(style));
    }

    public void string(String text) {
        styledString(text, Style.EMPTY);
    }

    public void translate(String key, Object... objects) {
        styledTranslate(key, Style.EMPTY, objects);
    }

    public List<Component> getLines() {
        return lines;
    }
}
