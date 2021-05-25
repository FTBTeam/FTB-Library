package dev.ftb.mods.ftblibrary.snbt;

import me.shedaniel.architectury.utils.NbtType;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SNBTParser {
	public static OrderedCompoundTag read(BufferedReader reader) throws IOException {
		SNBTParser parser = new SNBTParser(reader);
		return (OrderedCompoundTag) parser.readTag(parser.nextNS());
	}

	private static boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private final char[] buffer;
	private int position;

	private SNBTParser(BufferedReader r) throws IOException {
		StringBuilder bufferBuilder = new StringBuilder();

		String line;

		while ((line = r.readLine()) != null) {
			String tline = line.trim();

			if (!tline.startsWith("//") && !tline.startsWith("#")) {
				bufferBuilder.append(line);
			}

			bufferBuilder.append('\n');
		}

		buffer = bufferBuilder.toString().toCharArray();

		if (buffer.length < 2) {
			throw new SNBTSyntaxException("File has to have at least two characters!");
		}

		position = 0;
	}

	private String posString() {
		return posString(position);
	}

	private String posString(int p) {
		if (p >= buffer.length) {
			return "EOF";
		}

		int row = 0;
		int col = 0;

		for (int i = 0; i < p; i++) {
			if (buffer[i] == '\n') {
				row++;
				col = 0;
			} else {
				col++;
			}
		}

		return (row + 1) + ":" + (col + 1);
	}

	private char next() {
		if (position >= buffer.length) {
			throw new SNBTEOFException();
		}

		char c = buffer[position];
		position++;
		return c;
	}

	private char nextNS() {
		while (true) {
			char c = next();

			if (c > ' ') {
				return c;
			}
		}
	}

	private int getNumberType(String s) {
		if (s.length() == 0) {
			return 0;
		}

		char last = s.charAt(s.length() - 1);

		if (isDigit(last)) {
			return NbtType.INT;
		} else if (last == 'B' || last == 'b') {
			if (isInt(s, 1)) {
				return NbtType.BYTE;
			}
		} else if (last == 'S' || last == 's') {
			if (isInt(s, 1)) {
				return NbtType.SHORT;
			}
		} else if (last == 'L' || last == 'l') {
			if (isInt(s, 1)) {
				return NbtType.LONG;
			}
		} else if (last == 'F' || last == 'f') {
			if (isFloat(s, 1)) {
				return NbtType.FLOAT;
			}
		} else if (last == 'D' || last == 'd') {
			if (isFloat(s, 1)) {
				return NbtType.DOUBLE;
			}
		} else if (isInt(s, 0)) {
			return NbtType.INT;
		} else if (isFloat(s, 0)) {
			return -NbtType.DOUBLE;
		}

		return 0;
	}

	private boolean isInt(String s, int off) {
		int len = s.length() - off;

		if (len <= 0) {
			return false;
		}

		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);

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

	private boolean isFloat(String s, int off) {
		int len = s.length() - off;

		if (len <= 0) {
			return false;
		}

		int p = 0;

		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);

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
			} else if (!isDigit(c)) {
				return false;
			}
		}

		return true;
	}

	private Tag readTag(char first) {
		int pos = position - 1;

		switch (first) {
			case '{':
				return readCompound();
			case '[':
				return readCollection();
			case '"':
				return StringTag.valueOf(readQuotedString('"'));
			case '\'':
				return StringTag.valueOf(readQuotedString('\''));
		}

		String s = readWordString(first);

		switch (s) {
			case "true":
				return ByteTag.valueOf(true);
			case "false":
				return ByteTag.valueOf(false);
			case "null":
				throw new SNBTSyntaxException("null @ " + posString(pos) + " isn't supported!");
			case "Infinity":
			case "+Infinity":
				return DoubleTag.valueOf(Double.POSITIVE_INFINITY);
			case "-Infinity":
				return DoubleTag.valueOf(Double.NEGATIVE_INFINITY);
			case "NaN":
				return DoubleTag.valueOf(Double.NaN);
		}

		switch (getNumberType(s)) {
			case NbtType.BYTE:
				return ByteTag.valueOf(Byte.parseByte(s.substring(0, s.length() - 1)));
			case NbtType.SHORT:
				return ShortTag.valueOf(Short.parseShort(s.substring(0, s.length() - 1)));
			case NbtType.INT:
				return IntTag.valueOf(Integer.parseInt(s));
			case NbtType.LONG:
				return LongTag.valueOf(Long.parseLong(s.substring(0, s.length() - 1)));
			case NbtType.FLOAT:
				return FloatTag.valueOf(Float.parseFloat(s.substring(0, s.length() - 1)));
			case NbtType.DOUBLE:
				return DoubleTag.valueOf(Double.parseDouble(s.substring(0, s.length() - 1)));
			case -NbtType.DOUBLE:
				return DoubleTag.valueOf(Double.parseDouble(s));
			default:
				return StringTag.valueOf(s);
		}
	}

	private OrderedCompoundTag readCompound() {
		OrderedCompoundTag tag = new OrderedCompoundTag();

		while (true) {
			char c = nextNS();

			if (c == '}') {
				return tag;
			} else if (c == ',' || c == '\n') {
				continue;
			}

			String key;

			if (c == '"') {
				key = readQuotedString('"');
			} else if (c == '\'') {
				key = readQuotedString('\'');
			} else {
				key = readWordString(c);
			}

			char n = nextNS();

			if (n == ':' || n == '=') {
				Tag t = readTag(nextNS());
				tag.put(key, t);
			} else {
				throw new SNBTSyntaxException("Expected ':', got '" + n + "' @ " + posString());
			}
		}
	}

	private CollectionTag<?> readCollection() {
		int prevPos = position;
		char next1 = nextNS();
		char next2 = nextNS();

		if (next2 == ';' && (next1 == 'I' || next1 == 'i' || next1 == 'L' || next1 == 'l' || next1 == 'B' || next1 == 'b')) {
			return readArray(prevPos, next1);
		} else {
			position = prevPos;
			return readList();
		}
	}

	private ListTag readList() {
		ListTag tag = new ListTag();

		while (true) {
			int prevPos = position;
			char c = nextNS();

			if (c == ']') {
				return tag;
			} else if (c == ',') {
				continue;
			}

			Tag t = readTag(c);

			try {
				tag.add(t);
			} catch (UnsupportedOperationException ex) {
				throw new SNBTSyntaxException("Unexpected tag '" + t + "' in list @ " + posString(prevPos) + " - can't mix two different tag types in a list!");
			}
		}
	}

	private CollectionTag<?> readArray(int pos, char type) {
		List<Number> listOfNumbers = new ArrayList<>();

		while (true) {
			char c = nextNS();

			if (c == ']') {
				switch (type) {
					case 'i':
					case 'I':
						return new IntArrayTag((List<Integer>) (List) listOfNumbers);
					case 'l':
					case 'L':
						return new LongArrayTag((List<Long>) (List) listOfNumbers);
					case 'b':
					case 'B':
						return new ByteArrayTag((List<Byte>) (List) listOfNumbers);
					default:
						throw new SNBTSyntaxException("Unknown array type: " + type + " @ " + posString(pos));
				}
			} else if (c == ',') {
				continue;
			}

			Tag t = readTag(c);

			if (t instanceof NumericTag) {
				switch (type) {
					case 'i':
					case 'I':
						listOfNumbers.add(((NumericTag) t).getAsInt());
						break;
					case 'l':
					case 'L':
						listOfNumbers.add(((NumericTag) t).getAsLong());
						break;
					case 'b':
					case 'B':
						listOfNumbers.add(((NumericTag) t).getAsByte());
						break;
				}
			} else {
				throw new SNBTSyntaxException("Unexpected tag '" + t + "' in list @ " + posString() + " - expected a numeric tag!");
			}
		}
	}

	private String readWordString(char first) {
		StringBuilder sb = new StringBuilder();
		sb.append(first);

		while (true) {
			char c = next();

			if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || isDigit(c) || c == '.' || c == '_' || c == '-' || c == '+') {
				sb.append(c);
			} else {
				position--;
				return sb.toString();
			}
		}
	}

	private String readQuotedString(char stop) {
		StringBuilder sb = new StringBuilder();
		boolean escape = false;

		while (true) {
			char c = next();

			if (c == '\n') {
				throw new SNBTSyntaxException("New line without closing string with " + stop + " @ " + posString(position - 1) + "!");
			} else if (escape) {
				switch (c) {
					case 'n':
						sb.append('\n');
						break;
					case 'r':
						sb.append('\r');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'b':
						sb.append('\b');
						break;
					default:
						sb.append(c);
				}
				escape = false;
			} else if (c == '\\') {
				escape = true;
			} else if (c == stop) {
				return sb.toString();
			} else {
				sb.append(c);
			}
		}
	}
}
