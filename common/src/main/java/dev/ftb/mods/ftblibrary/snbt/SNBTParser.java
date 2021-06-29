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

import java.util.ArrayList;
import java.util.List;

class SNBTParser {
	static SNBTCompoundTag read(List<String> lines) {
		SNBTParser parser = new SNBTParser(lines);
		return (SNBTCompoundTag) parser.readTag(parser.nextNS());
	}

	private final char[] buffer;
	private int position;

	private SNBTParser(List<String> lines) {
		StringBuilder bufferBuilder = new StringBuilder();

		for (String line : lines) {
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

		switch (SNBTUtils.getNumberType(s)) {
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

	private SNBTCompoundTag readCompound() {
		SNBTCompoundTag tag = new SNBTCompoundTag();

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

			if (SNBTUtils.isSimpleCharacter(c)) {
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
				escape = false;

				if (SNBTUtils.REVERSE_ESCAPE_CHARS[c] != 0) {
					sb.append(SNBTUtils.REVERSE_ESCAPE_CHARS[c]);
				}
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
