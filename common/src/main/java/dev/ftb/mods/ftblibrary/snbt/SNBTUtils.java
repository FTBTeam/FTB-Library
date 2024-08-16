package dev.ftb.mods.ftblibrary.snbt;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import net.minecraft.Util;
import net.minecraft.nbt.Tag;

import java.util.function.BooleanSupplier;

public class SNBTUtils {
    public static final BooleanSupplier ALWAYS_TRUE = () -> true;
    public static final char[] ESCAPE_CHARS = Util.make(new char[128], array -> {
        array['"'] = '\"';
        array['\\'] = '\\';
        array['\t'] = 't';
        array['\b'] = 'b';
        array['\n'] = 'n';
        array['\r'] = 'r';
        array['\f'] = 'f';
    });
    public static final char[] REVERSE_ESCAPE_CHARS = Util.make(new char[128], array -> {
        for (var i = 0; i < array.length; i++) {
            if (ESCAPE_CHARS[i] != 0) {
                array[ESCAPE_CHARS[i]] = (char) i;
            }
        }
    });

    public static boolean isSimpleCharacter(char c) {
        return Character.isAlphabetic(c) || Character.isDigit(c) || c == '.' || c == '_' || c == '-' || c == '+' || c == '∞';
    }

    public static boolean isSimpleString(String string) {
        for (var i = 0; i < string.length(); i++) {
            if (!isSimpleCharacter(string.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static int getNumberType(String s) {
        if (s.isEmpty()) {
            return Tag.TAG_STRING;
        }

        var last = Character.toLowerCase(s.charAt(s.length() - 1));

        if (Character.isDigit(last) && Ints.tryParse(s) != null) {
            return Tag.TAG_INT;
        }

        String start = s.substring(0, s.length() - 1);
        if (last == 'b' && Ints.tryParse(start) != null) {
            return Tag.TAG_BYTE;
        } else if (last == 's' && Ints.tryParse(start) != null) {
            return Tag.TAG_SHORT;
        } else if (last == 'l' && Longs.tryParse(start) != null) {
            return Tag.TAG_LONG;
        } else if (last == 'f' && Floats.tryParse(start) != null) {
            return Tag.TAG_FLOAT;
        } else if (last == 'd' && Doubles.tryParse(start) != null) {
            return Tag.TAG_DOUBLE;
        } else if (Floats.tryParse(s) != null) {
            return -Tag.TAG_DOUBLE;
        } else {
            return Tag.TAG_STRING;
        }
    }

    public static String handleEscape(String string) {
        return isSimpleString(string) ? string : quoteAndEscape(string);
    }

    public static String quoteAndEscape(String string) {
        var len = string.length();
        var sb = new StringBuilder(len + 2);
        sb.append('"');

        for (var i = 0; i < len; i++) {
            var c = string.charAt(i);

            if (c < ESCAPE_CHARS.length && ESCAPE_CHARS[c] != 0) {
                sb.append('\\');
                sb.append(ESCAPE_CHARS[c]);
            } else {
                sb.append(c);
            }
        }

        sb.append('"');
        return sb.toString();
    }
}
