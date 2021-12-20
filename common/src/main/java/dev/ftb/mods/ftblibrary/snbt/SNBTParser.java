package dev.ftb.mods.ftblibrary.snbt;

import dev.architectury.utils.NbtType;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
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
		var parser = new SNBTParser(lines);
		return (SNBTCompoundTag) SpecialTag.unwrap(parser.readTag(parser.nextNS()));
	}

	private final char[] buffer;
	private int position;

	private SNBTParser(List<String> lines) {
		var bufferBuilder = new StringBuilder();

		for (var line : lines) {
			var tline = line.trim();

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

		var row = 0;
		var col = 0;

		for (var i = 0; i < p; i++) {
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

		var c = buffer[position];
		position++;
		return c;
	}

	private char nextNS() {
		while (true) {
			var c = next();

			if (c > ' ') {
				return c;
			}
		}
	}

	private Tag readTag(char first) {
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

		var s = readWordString(first);

		return switch (s) {
			case "true" -> SpecialTag.TRUE;
			case "false" -> SpecialTag.FALSE;
			case "null", "end", "END" -> EndTag.INSTANCE;
			case "Infinity", "Infinityd", "+Infinity", "+Infinityd", "∞", "∞d", "+∞", "+∞d" -> SpecialTag.POS_INFINITY_D;
			case "-Infinity", "-Infinityd", "-∞", "-∞d" -> SpecialTag.NEG_INFINITY_D;
			case "NaN", "NaNd" -> SpecialTag.NAN_D;
			case "Infinityf", "+Infinityf", "∞f", "+∞f" -> SpecialTag.POS_INFINITY_F;
			case "-Infinityf", "-∞f" -> SpecialTag.NEG_INFINITY_F;
			case "NaNf" -> SpecialTag.NAN_F;
			default -> switch (SNBTUtils.getNumberType(s)) {
				case NbtType.BYTE -> ByteTag.valueOf(Byte.parseByte(s.substring(0, s.length() - 1)));
				case NbtType.SHORT -> ShortTag.valueOf(Short.parseShort(s.substring(0, s.length() - 1)));
				case NbtType.INT -> IntTag.valueOf(Integer.parseInt(s));
				case NbtType.LONG -> LongTag.valueOf(Long.parseLong(s.substring(0, s.length() - 1)));
				case NbtType.FLOAT -> FloatTag.valueOf(Float.parseFloat(s.substring(0, s.length() - 1)));
				case NbtType.DOUBLE -> DoubleTag.valueOf(Double.parseDouble(s.substring(0, s.length() - 1)));
				case -NbtType.DOUBLE -> DoubleTag.valueOf(Double.parseDouble(s));
				default -> StringTag.valueOf(s);
			};
		};

	}

	private SNBTCompoundTag readCompound() {
		var tag = new SNBTCompoundTag();

		while (true) {
			var c = nextNS();

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

			var n = nextNS();

			if (n == ':' || n == '=') {
				var t = readTag(nextNS());

				if (t == SpecialTag.TRUE) {
					tag.getOrCreateProperties(key).valueType = SNBTTagProperties.TYPE_TRUE;
				} else if (t == SpecialTag.FALSE) {
					tag.getOrCreateProperties(key).valueType = SNBTTagProperties.TYPE_FALSE;
				}

				tag.put(key, SpecialTag.unwrap(t));
			} else {
				throw new SNBTSyntaxException("Expected ':', got '" + n + "' @ " + posString());
			}
		}
	}

	private CollectionTag<?> readCollection() {
		var prevPos = position;
		var next1 = nextNS();
		var next2 = nextNS();

		if (next2 == ';' && (next1 == 'I' || next1 == 'i' || next1 == 'L' || next1 == 'l' || next1 == 'B' || next1 == 'b')) {
			return readArray(prevPos, next1);
		} else {
			position = prevPos;
			return readList();
		}
	}

	private ListTag readList() {
		var tag = new ListTag();

		while (true) {
			var prevPos = position;
			var c = nextNS();

			if (c == ']') {
				return tag;
			} else if (c == ',') {
				continue;
			}

			var t = SpecialTag.unwrap(readTag(c));

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
			var c = nextNS();

			if (c == ']') {
				return switch (type) {
					case 'i', 'I' -> new IntArrayTag((List<Integer>) (List) listOfNumbers);
					case 'l', 'L' -> new LongArrayTag((List<Long>) (List) listOfNumbers);
					case 'b', 'B' -> new ByteArrayTag((List<Byte>) (List) listOfNumbers);
					default -> throw new SNBTSyntaxException("Unknown array type: " + type + " @ " + posString(pos));
				};
			} else if (c == ',') {
				continue;
			}

			var t = SpecialTag.unwrap(readTag(c));

			if (t instanceof NumericTag) {
				switch (type) {
					case 'i', 'I' -> listOfNumbers.add(((NumericTag) t).getAsInt());
					case 'l', 'L' -> listOfNumbers.add(((NumericTag) t).getAsLong());
					case 'b', 'B' -> listOfNumbers.add(((NumericTag) t).getAsByte());
				}
			} else {
				throw new SNBTSyntaxException("Unexpected tag '" + t + "' in list @ " + posString() + " - expected a numeric tag!");
			}
		}
	}

	private String readWordString(char first) {
		var sb = new StringBuilder();
		sb.append(first);

		while (true) {
			var c = next();

			if (SNBTUtils.isSimpleCharacter(c)) {
				sb.append(c);
			} else {
				position--;
				return sb.toString();
			}
		}
	}

	private String readQuotedString(char stop) {
		var sb = new StringBuilder();
		var escape = false;

		while (true) {
			var c = next();

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
