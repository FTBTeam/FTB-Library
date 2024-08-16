package dev.ftb.mods.ftblibrary.util;

import dev.ftb.mods.ftblibrary.math.Bits;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class StringUtils {
    public static final String ALLOWED_TEXT_CHARS = " .-_!@#$%^&*()+=\\/,<>?'\"[]{}|;:`~";
    public static final char FORMATTING_CHAR = '\u00a7';
    public static final String[] EMPTY_ARRAY = {};
    public static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    public static final Predicate<String> ALWAYS_TRUE = s -> true;

    public static final int FLAG_ID_ALLOW_EMPTY = 1;
    public static final int FLAG_ID_FIX = 2;
    public static final int FLAG_ID_ONLY_LOWERCASE = 4;
    public static final int FLAG_ID_ONLY_UNDERLINE = 8;
    public static final int FLAG_ID_ONLY_UNDERLINE_OR_PERIOD = FLAG_ID_ONLY_UNDERLINE | 16;
    public static final int FLAG_ID_DEFAULTS = FLAG_ID_FIX | FLAG_ID_ONLY_LOWERCASE | FLAG_ID_ONLY_UNDERLINE;

    public static final Comparator<Object> IGNORE_CASE_COMPARATOR = (o1, o2) -> String.valueOf(o1).compareToIgnoreCase(String.valueOf(o2));
    public static final Comparator<Object> ID_COMPARATOR = (o1, o2) -> getID(o1, FLAG_ID_FIX).compareToIgnoreCase(getID(o2, FLAG_ID_FIX));

    public static final Map<String, String> TEMP_MAP = new HashMap<>();
    public static final DecimalFormat DOUBLE_FORMATTER_00 = Util.make(new DecimalFormat("#0.00"), f -> f.setRoundingMode(RoundingMode.DOWN));
    public static final DecimalFormat DOUBLE_FORMATTER_0 = Util.make(new DecimalFormat("#0.0"), f -> f.setRoundingMode(RoundingMode.DOWN));
    public final static int[] INT_SIZE_TABLE = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};

    private static final Pattern NOT_SNAKE_CASE_PATTERN = Pattern.compile("[^a-z0-9_]");
    private static final Pattern REPEATING_UNDERSCORE_PATTERN = Pattern.compile("_{2,}");
    private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)[\\&\u00a7]([0-9A-FK-OR])");

    public static boolean ignoreResourceLocationErrors = false;

    public static String unformatted(String string) {
        return string.isEmpty() ? string : FORMATTING_CODE_PATTERN.matcher(string).replaceAll("");
    }

    public static String addFormatting(String string) {
        return FORMATTING_CODE_PATTERN.matcher(string).replaceAll("\u00a7$1");
    }

    public static String toSnakeCase(String string) {
        return string.isEmpty() ? string : REPEATING_UNDERSCORE_PATTERN.matcher(NOT_SNAKE_CASE_PATTERN.matcher(unformatted(string).toLowerCase()).replaceAll("_")).replaceAll("_");
    }

    public static String emptyIfNull(@Nullable Object o) {
        return o == null ? "" : o.toString();
    }

    public static String getRawID(Object o) {
        if (o instanceof StringRepresentable) {
            return ((StringRepresentable) o).getSerializedName();
        } else if (o instanceof Enum) {
            return ((Enum) o).name();
        }

        return String.valueOf(o);
    }

    public static String getID(Object o, int flags) {
        var id = getRawID(o);

        if (flags == 0) {
            return id;
        }

        var fix = Bits.getFlag(flags, FLAG_ID_FIX);

        if (!fix && id.isEmpty() && !Bits.getFlag(flags, FLAG_ID_ALLOW_EMPTY)) {
            throw new NullPointerException("ID can't be empty!");
        }

        if (Bits.getFlag(flags, FLAG_ID_ONLY_LOWERCASE)) {
            if (fix) {
                id = id.toLowerCase();
            } else if (!id.equals(id.toLowerCase())) {
                throw new IllegalArgumentException("ID can't contain uppercase characters!");
            }
        }

        if (Bits.getFlag(flags, FLAG_ID_ONLY_UNDERLINE)) {
            if (fix) {
                id = id.toLowerCase();
            } else if (!id.equals(id.toLowerCase())) {
                throw new IllegalArgumentException("ID can't contain uppercase characters!");
            }
        }

        if (Bits.getFlag(flags, FLAG_ID_ONLY_UNDERLINE)) {
            var allowPeriod = Bits.getFlag(flags, 16);

            var chars = id.toCharArray();

            for (var i = 0; i < chars.length; i++) {
                if (!(chars[i] == '.' && allowPeriod || isTextChar(chars[i], true))) {
                    if (fix) {
                        chars[i] = '_';
                    } else {
                        throw new IllegalArgumentException("ID contains invalid character: '" + chars[i] + "'!");
                    }
                }
            }

            id = new String(chars);
        }

        return id;
    }

    public static String[] shiftArray(@Nullable String[] s) {
        if (s == null || s.length <= 1) {
            return EMPTY_ARRAY;
        }

        var s1 = new String[s.length - 1];
        System.arraycopy(s, 1, s1, 0, s1.length);
        return s1;
    }

    public static boolean isASCIIChar(char c) {
        return c > 0 && c < 256;
    }

    public static boolean isTextChar(char c, boolean onlyAZ09) {
        return isASCIIChar(c) && (c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || !onlyAZ09 && (ALLOWED_TEXT_CHARS.indexOf(c) != -1));
    }

    public static void replace(List<String> txt, String s, String s1) {
        if (!txt.isEmpty()) {
            String s2;
            for (var i = 0; i < txt.size(); i++) {
                s2 = txt.get(i);
                if (s2 != null && s2.length() > 0) {
                    s2 = s2.replace(s, s1);
                    txt.set(i, s2);
                }
            }
        }
    }

    public static String replace(String s, char c, char with) {
        if (s.isEmpty()) {
            return s;
        }

        var sb = new StringBuilder();
        for (var i = 0; i < s.length(); i++) {
            var c1 = s.charAt(i);
            sb.append((c1 == c) ? with : c1);
        }

        return sb.toString();
    }

    public static String joinSpaceUntilEnd(int startIndex, CharSequence[] o) {
        if (startIndex < 0 || o.length <= startIndex) {
            return "";
        }

        var sb = new StringBuilder();

        for (var i = startIndex; i < o.length; i++) {
            sb.append(o[i]);
            if (i != o.length - 1) {
                sb.append(' ');
            }
        }

        return sb.toString();
    }

    public static String firstUppercase(String s) {
        if (s.length() == 0) {
            return s;
        }
        var c = Character.toUpperCase(s.charAt(0));
        if (s.length() == 1) {
            return Character.toString(c);
        }
        return c + s.substring(1);
    }

    public static String fillString(CharSequence s, char fill, int length) {
        var sl = s.length();

        var c = new char[Math.max(sl, length)];

        for (var i = 0; i < c.length; i++) {
            if (i >= sl) {
                c[i] = fill;
            } else {
                c[i] = s.charAt(i);
            }
        }

        return new String(c);
    }

    public static String removeAllWhitespace(String s) {
        var chars = new char[s.length()];
        var j = 0;

        for (var i = 0; i < chars.length; i++) {
            var c = s.charAt(i);

            if (c > ' ') {
                chars[j] = c;
                j++;
            }
        }

        return new String(chars, 0, j);
    }

    public static String formatDouble0(double value) {
        return (value == (long) value) ? String.format("%,d", (long) value) : DOUBLE_FORMATTER_0.format(value);
    }

    public static String formatDouble00(double value) {
        return (value == (long) value) ? String.format("%,d", (long) value) : DOUBLE_FORMATTER_00.format(value);
    }

    public static String formatDouble(double value, boolean fancy) {
        if (Double.isNaN(value)) {
            return "NaN";
        } else if (value == Double.POSITIVE_INFINITY) {
            return "+Inf";
        } else if (value == Double.NEGATIVE_INFINITY) {
            return "-Inf";
        } else if (value == Long.MAX_VALUE) {
            return "2^63-1";
        } else if (value == Long.MIN_VALUE) {
            return "-2^63";
        } else if (value == 0D) {
            return "0";
        } else if (!fancy) {
            return formatDouble00(value);
        } else if (value >= 1000000000D) {
            return formatDouble00(value / 1000000000D) + "B";
        } else if (value >= 1000000D) {
            return formatDouble00(value / 1000000D) + "M";
        } else if (value >= 10000D) {
            return formatDouble00(value / 1000D) + "K";
        }

        return formatDouble00(value);
    }

    public static String formatDouble(double value) {
        return formatDouble(value, false);
    }

    public static Map<String, String> parse(Map<String, String> map, String s) {
        if (map == TEMP_MAP) {
            map.clear();
        }

        for (var entry : s.split(",")) {
            var val = entry.split("=");

            for (var key : val[0].split("&")) {
                map.put(key, val[1]);
            }
        }

        return map;
    }

    //FIXME
    public static String fixTabs(String string, int tabSize) {
        String with;

        if (tabSize == 2) {
            with = "  ";
        } else if (tabSize == 4) {
            with = "    ";
        } else {
            var c = new char[tabSize];
            Arrays.fill(c, ' ');
            with = new String(c);
        }

        return string.replace("\t", with);
    }

    public static int stringSize(int x) {
        for (var i = 0; ; i++) {
            if (x <= INT_SIZE_TABLE[i]) {
                return i + 1;
            }
        }
    }

    public static String add0s(int number, int max) {
        var size = stringSize(max);
        var nsize = stringSize(number);

        return "0".repeat(Math.max(0, size - nsize)) + number;
    }

    public static String camelCaseToWords(String key) {
        var builder = new StringBuilder();
        var pu = false;

        for (var i = 0; i < key.length(); i++) {
            var c = key.charAt(i);
            var u = Character.isUpperCase(c);

            if (!pu && u) {
                builder.append(' ');
            }

            pu = u;

            if (i == 0) {
                c = Character.toUpperCase(c);
            }

            builder.append(c);
        }

        return builder.toString();
    }

    public static Map<String, String> splitProperties(String s) {
        Map<String, String> map = new LinkedHashMap<>();

        for (var s1 : s.split(" ")) {
            if (!s1.isEmpty()) {
                var s2 = s1.split(":", 2);
                map.put(s2[0], s2.length == 2 ? s2[1].replace("%20", " ") : "");
            }
        }

        return map;
    }
}