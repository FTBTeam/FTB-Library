package dev.ftb.mods.ftblibrary.math;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Made by LatvianModder
 */
public class Bits {
	private static final int MAX_BYTE = 0xFF;
	private static final int MAX_SHORT = 0xFFFF;
	private static final long MAX_INT = 0xFFFFFFFFL;

	public static boolean getFlag(int flags, int flag) {
		return (flags & flag) != 0;
	}

	public static int setFlag(int flags, int flag, boolean v) {
		if (v) {
			return flags | flag;
		} else {
			return flags & ~flag;
		}
	}

	public static int toInt(boolean[] b) {
		var d = 0;
		for (var i = 0; i < b.length; i++) {
			d |= (b[i] ? 1 : 0) << i;
		}
		return d;
	}

	public static void toBool(boolean[] b, int d) {
		for (var j = 0; j < b.length; j++) {
			b[j] = ((d >> j) & 1) == 1;
		}
	}

	//

	//Int
	public static long intsToLong(int a, int b) {
		return (((long) a) << 32) | (b & MAX_INT);
	}

	public static int intFromLongA(long l) {
		return (int) (l >> 32);
	}

	public static int intFromLongB(long l) {
		return (int) l;
	}

	//Short
	public static int shortsToInt(int a, int b) {
		return ((short) a << 16) | ((short) b & MAX_SHORT);
	}

	public static short shortFromIntA(int i) {
		return (short) (i >> 16);
	}

	public static short shortFromIntB(int i) {
		return (short) (i & MAX_SHORT);
	}

	//Byte
	public static short bytesToShort(int a, int b) {
		return (short) (((a & MAX_BYTE) << 8) | (b & MAX_BYTE));
	}

	public static byte byteFromShortA(short s) {
		return (byte) ((s >> 8) & MAX_BYTE);
	}

	public static byte byteFromShortB(short s) {
		return (byte) (s & MAX_BYTE);
	}

	// - //

	public static int toUShort(byte[] b, int off) {
		var ch1 = b[off] & MAX_BYTE;
		var ch2 = b[off + 1] & MAX_BYTE;
		return (ch1 << 8) + ch2;
	}

	public static int toInt(byte[] b, int off) {
		var ch1 = b[off] & MAX_BYTE;
		var ch2 = b[off + 1] & MAX_BYTE;
		var ch3 = b[off + 2] & MAX_BYTE;
		var ch4 = b[off + 3] & MAX_BYTE;
		return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4;
	}

	public static long toLong(byte[] b, int off) {
		return (((long) b[off] << 56) + ((long) (b[off + 1] & MAX_BYTE) << 48) + ((long) (b[off + 2] & MAX_BYTE) << 40) + ((long) (b[off + 3] & MAX_BYTE) << 32) + ((long) (b[off + 4] & MAX_BYTE) << 24) + ((b[off + 5] & MAX_BYTE) << 16) + ((b[off + 6] & MAX_BYTE) << 8) + ((b[off + 7] & MAX_BYTE)));
	}

	public static UUID toUUID(byte[] b, int off) {
		var msb = toLong(b, off);
		var lsb = toLong(b, off + 8);
		return new UUID(msb, lsb);
	}

	public static List<UUID> toUUIDList(byte[] b) {
		if (b.length == 0) {
			return Collections.emptyList();
		}

		List<UUID> list = new ArrayList<>(b.length / 16);

		for (var i = 0; i < b.length; i += 16) {
			list.add(toUUID(b, i));
		}

		return list;
	}

	// - //

	public static void fromUShort(byte[] b, int off, int v) {
		b[off] = (byte) (v >>> 8);
		b[off + 1] = (byte) v;
	}

	public static void fromInt(byte[] b, int off, int v) {
		b[off] = (byte) (v >>> 24);
		b[off + 1] = (byte) (v >>> 16);
		b[off + 2] = (byte) (v >>> 8);
		b[off + 3] = (byte) v;
	}

	public static void fromLong(byte[] b, int off, long v) {
		b[off] = (byte) (v >>> 56);
		b[off + 1] = (byte) (v >>> 48);
		b[off + 2] = (byte) (v >>> 40);
		b[off + 3] = (byte) (v >>> 32);
		b[off + 4] = (byte) (v >>> 24);
		b[off + 5] = (byte) (v >>> 16);
		b[off + 6] = (byte) (v >>> 8);
		b[off + 7] = (byte) v;
	}

	public static void fromUUID(byte[] b, int off, UUID uuid) {
		fromLong(b, off, uuid.getMostSignificantBits());
		fromLong(b, off + 8, uuid.getLeastSignificantBits());
	}

	public static byte[] fromUUIDList(Collection<UUID> c) {
		var b = new byte[c.size() * 16];
		var idx = 0;

		for (var id : c) {
			fromUUID(b, idx, id);
			idx += 16;
		}

		return b;
	}
}