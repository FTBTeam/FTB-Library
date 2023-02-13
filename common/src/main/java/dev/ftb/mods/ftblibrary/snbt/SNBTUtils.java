package dev.ftb.mods.ftblibrary.snbt;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import net.minecraft.nbt.Tag;

import java.util.function.BooleanSupplier;

public class SNBTUtils {
	public static final BooleanSupplier ALWAYS_TRUE = () -> true;
	public static final char[] ESCAPE_CHARS = new char[128];
	public static final char[] REVERSE_ESCAPE_CHARS = new char[128];

	static {
		ESCAPE_CHARS['"'] = '\"';
		ESCAPE_CHARS['\\'] = '\\';
		ESCAPE_CHARS['\t'] = 't';
		ESCAPE_CHARS['\b'] = 'b';
		ESCAPE_CHARS['\n'] = 'n';
		ESCAPE_CHARS['\r'] = 'r';
		ESCAPE_CHARS['\f'] = 'f';

		for (var i = 0; i < ESCAPE_CHARS.length; i++) {
			if (ESCAPE_CHARS[i] != 0) {
				REVERSE_ESCAPE_CHARS[ESCAPE_CHARS[i]] = (char) i;
			}
		}
	}

	public static boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	public static boolean isSimpleCharacter(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || isDigit(c) || c == '.' || c == '_' || c == '-' || c == '+' || c == '∞';
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

	@Deprecated(forRemoval = true)
	public static boolean isInt(String s, int off) {
		var len = s.length() - off;

		if (len <= 0) {
			return false;
		}

		for (var i = 0; i < len; i++) {
			var c = s.charAt(i);

			if (c == '-') {
				if (i != 0) {
					return false;
				}
			} else if (!isDigit(c)) {
				return false;
			}
		}

		return true;
	}

	@Deprecated(forRemoval = true)
	public static boolean isFloat(String s, int off) {
		var len = s.length() - off;

		if (len <= 0) {
			return false;
		}

		var p = 0;
		var e = 0;

		for (var i = 0; i < len; i++) {
			var c = s.charAt(i);

			if (c == '-') {
				if (i != 0) {
					return false;
				}
			} else if (c == '.') {
				if (i == 0 || i == len - 1) {
					return false;
				}

				p++;

				if (p >= 2) {
					return false;
				}
			} else if (c == 'E') {
				if (i == 0 || i == len - 1) {
					return false;
				}

				e++;

				if (e >= 2) {
					return false;
				}
			} else if (!isDigit(c)) {
				return false;
			}
		}

		return true;
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
