package dev.ftb.mods.ftblibrary.util;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class UUIDUtils {
	public static String toString(@Nullable UUID id) {
		if (id != null) {
			var msb = id.getMostSignificantBits();
			var lsb = id.getLeastSignificantBits();
			var sb = new StringBuilder(32);
			digitsUUID(sb, msb >> 32, 8);
			digitsUUID(sb, msb >> 16, 4);
			digitsUUID(sb, msb, 4);
			digitsUUID(sb, lsb >> 48, 4);
			digitsUUID(sb, lsb, 12);
			return sb.toString();
		}

		return "";
	}

	private static void digitsUUID(StringBuilder sb, long val, int digits) {
		var hi = 1L << (digits * 4);
		var s = Long.toHexString(hi | (val & (hi - 1)));
		sb.append(s, 1, s.length());
	}

	@Nullable
	public static UUID fromString(@Nullable String s) {
		if (s == null || !(s.length() == 32 || s.length() == 36)) {
			return null;
		}

		try {
			if (s.indexOf('-') != -1) {
				return UUID.fromString(s);
			}

			var l = s.length();
			var sb = new StringBuilder(36);
			for (var i = 0; i < l; i++) {
				sb.append(s.charAt(i));
				if (i == 7 || i == 11 || i == 15 || i == 19) {
					sb.append('-');
				}
			}

			return UUID.fromString(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
