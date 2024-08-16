package dev.ftb.mods.ftblibrary.util;

import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;


public class TextComponentParser {
    public static final Char2ObjectOpenHashMap<ChatFormatting> CODE_TO_FORMATTING = Util.make(new Char2ObjectOpenHashMap<>(), map -> {
        map.put('0', ChatFormatting.BLACK);
        map.put('1', ChatFormatting.DARK_BLUE);
        map.put('2', ChatFormatting.DARK_GREEN);
        map.put('3', ChatFormatting.DARK_AQUA);
        map.put('4', ChatFormatting.DARK_RED);
        map.put('5', ChatFormatting.DARK_PURPLE);
        map.put('6', ChatFormatting.GOLD);
        map.put('7', ChatFormatting.GRAY);
        map.put('8', ChatFormatting.DARK_GRAY);
        map.put('9', ChatFormatting.BLUE);
        map.put('a', ChatFormatting.GREEN);
        map.put('b', ChatFormatting.AQUA);
        map.put('c', ChatFormatting.RED);
        map.put('d', ChatFormatting.LIGHT_PURPLE);
        map.put('e', ChatFormatting.YELLOW);
        map.put('f', ChatFormatting.WHITE);
        map.put('k', ChatFormatting.OBFUSCATED);
        map.put('l', ChatFormatting.BOLD);
        map.put('m', ChatFormatting.STRIKETHROUGH);
        map.put('n', ChatFormatting.UNDERLINE);
        map.put('o', ChatFormatting.ITALIC);
        map.put('r', ChatFormatting.RESET);
    });

    private final String text;
    private final Function<String, Component> substitutes;
    private MutableComponent component;
    private StringBuilder builder;
    private Style style;

    private TextComponentParser(String txt, @Nullable Function<String, Component> sub) {
        text = txt;
        substitutes = sub;
    }

    public static Component parse(String text, @Nullable Function<String, Component> substitutes) {
        var c = parse0(text, substitutes);

        if (c == Component.EMPTY) {
            return c;
        }

        while (c.getContents() == PlainTextContents.EMPTY && c.getStyle().equals(Style.EMPTY) && c.getSiblings().size() == 1) {
            c = c.getSiblings().get(0);
        }

        return c;
    }

    private static Component parse0(String text, @Nullable Function<String, Component> substitutes) {
        try {
            return new TextComponentParser(text, substitutes).parse();
        } catch (BadFormatException ex) {
            return Component.literal(ex.getMessage()).withStyle(ChatFormatting.RED);
        } catch (Exception ex) {
            return Component.literal(ex.toString()).withStyle(ChatFormatting.RED);
        }
    }

    private Component parse() throws BadFormatException {
        if (text.isEmpty()) {
            return Component.empty();
        }

        var c = text.replaceAll("\\\\n", "\n").toCharArray();
        var hasSpecialCodes = false;

        for (var c1 : c) {
            if (c1 == '{' || c1 == '&' || c1 == '\u00a7') {
                hasSpecialCodes = true;
                break;
            }
        }

        if (!hasSpecialCodes) {
            return Component.literal(new String(c));
        }

        component = Component.literal("");
        style = Style.EMPTY;
        builder = new StringBuilder();
        var sub = false;

        for (var i = 0; i < c.length; i++) {
            var escape = i > 0 && c[i - 1] == '\\';
            var end = i == c.length - 1;

            if (sub && (end || c[i] == '{' || c[i] == '}')) {
                if (c[i] == '{') {
                    throw new BadFormatException("Invalid formatting! Can't nest multiple substitutes!");
                }

                finishPart();
                sub = false;
                continue;
            }

            if (!escape) {
                if (c[i] == '&' || c[i] == '\u00a7') {
                    finishPart();

                    if (end) {
                        throw new BadFormatException("Invalid formatting! Can't end string with &!");
                    }

                    i++;

                    if (c[i] == '#') {
                        var rrggbb = new char[7];
                        rrggbb[0] = '#';
                        System.arraycopy(c, i + 1, rrggbb, 1, 6);
                        i += 6;
                        style = style.withColor(TextColor.parseColor(new String(rrggbb)).result().orElse(TextColor.fromRgb(0xFFFFFF)));
                    } else {
                        if (c[i] == ' ') {
                            throw new BadFormatException("Invalid formatting! You must escape whitespace after & with \\&!");
                        }

                        var formatting = CODE_TO_FORMATTING.get(c[i]);

                        if (formatting == null) {
                            throw new BadFormatException("Invalid formatting! Unknown formatting symbol after &: '" + c[i] + "'!");
                        }

                        style = style.applyFormat(formatting);
                    }

                    continue;
                } else if (c[i] == '{') {
                    finishPart();

                    if (end) {
                        throw new BadFormatException("Invalid formatting! Can't end string with {!");
                    }

                    sub = true;
                }
            }

            if (c[i] != '\\' || escape) {
                builder.append(c[i]);
            }
        }

        finishPart();
        return component;
    }

    private void finishPart() throws BadFormatException {
        var string = builder.toString();
        builder.setLength(0);

        if (string.isEmpty()) {
            return;
        } else if (string.length() < 2 || string.charAt(0) != '{') {
            var component1 = Component.literal(string);
            component1.setStyle(style);
            component.append(component1);
            return;
        }

        var component1 = substitutes.apply(string.substring(1));

        if (component1 != null) {
            var style0 = component1.getStyle();
            var style1 = style;
            style1 = style1.withHoverEvent(style0.getHoverEvent());
            style1 = style1.withClickEvent(style0.getClickEvent());
            style1 = style1.withInsertion(style0.getInsertion());
            component1 = Component.literal("").append(component1).withStyle(style1);
        } else {
            throw new BadFormatException("Invalid formatting! Unknown substitute: " + string.substring(1));
        }

        component.append(component1);
    }

    private static class BadFormatException extends IllegalArgumentException {
        private BadFormatException(String s) {
            super(s);
        }
    }
}
