package dev.ftb.mods.ftblibrary.snbt;

import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class SNBT {
	private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");

	public static String handleEscape(String string) {
		return SIMPLE_VALUE.matcher(string).matches() ? string : StringTag.quoteAndEscape(string);
	}

	public static boolean getSingleLine(Tag tag) {
		return tag instanceof OrderedCompoundTag && ((OrderedCompoundTag) tag).singleLine;

		// TODO: Check lists and other collections somehow, possibly a mixin
	}

	@Nullable
	public static CompoundTag readLines(List<String> lines) {
		StringBuilder s = new StringBuilder();

		try {
			for (String line : lines) {
				s.append(line.trim());
			}

			return TagParser.parseTag(s.toString());
		} catch (Exception ex) {
		}

		return null;
	}

	@Nullable
	public static CompoundTag read(Path path) {
		if (Files.notExists(path)) {
			return null;
		}

		try {
			return readLines(Files.readAllLines(path, StandardCharsets.UTF_8));
		} catch (Exception ex) {
			return null;
		}
	}

	public static List<String> writeLines(CompoundTag nbt) {
		SNBTBuilder builder = new SNBTBuilder();
		append(builder, nbt);
		builder.println();
		return builder.lines;
	}

	public static boolean write(Path path, CompoundTag nbt) {
		try {
			if (Files.notExists(path.getParent())) {
				Files.createDirectories(path.getParent());
			}

			Files.write(path, writeLines(nbt));
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private static void append(SNBTBuilder builder, @Nullable Tag nbt) {
		if (nbt == null || nbt instanceof EndTag) {
			builder.print("null");
		} else if (nbt instanceof CompoundTag) {
			CompoundTag compound = (CompoundTag) nbt;

			if (compound.isEmpty()) {
				builder.print("{ }");
				return;
			}

			boolean singleLine = getSingleLine(nbt);

			builder.print("{");

			if (singleLine) {
				builder.print(" ");
			} else {
				builder.println();
				builder.push();
			}

			int index = 0;

			for (String key : compound.getAllKeys()) {
				index++;
				builder.print(handleEscape(key));
				builder.print(": ");

				if (compound instanceof OrderedCompoundTag && ((OrderedCompoundTag) compound).booleanKeys != null && ((OrderedCompoundTag) compound).booleanKeys.contains(key)) {
					builder.print(compound.getBoolean(key) ? "true" : "false");
				} else {
					append(builder, compound.get(key));
				}

				if (index != compound.size()) {
					builder.print(",");
				}

				if (singleLine) {
					builder.print(" ");
				} else {
					builder.println();
				}
			}

			if (!singleLine) {
				builder.pop();
			}

			builder.print("}");
		} else if (nbt instanceof CollectionTag) {
			if (nbt instanceof ByteArrayTag) {
				appendCollection(builder, (CollectionTag<?>) nbt, "B;");
			} else if (nbt instanceof IntArrayTag) {
				appendCollection(builder, (CollectionTag<?>) nbt, "I;");
			} else if (nbt instanceof LongArrayTag) {
				appendCollection(builder, (CollectionTag<?>) nbt, "L;");
			} else {
				appendCollection(builder, (CollectionTag<?>) nbt, "");
			}
		} else {
			builder.print(nbt.toString());
		}
	}

	private static void appendCollection(SNBTBuilder builder, CollectionTag<? extends Tag> nbt, String opening) {
		if (nbt.isEmpty()) {
			builder.print("[");
			builder.print(opening);
			builder.print(" ]");
			return;
		} else if (nbt.size() == 1) {
			builder.print("[");
			builder.print(opening);
			append(builder, nbt.get(0));
			builder.print("]");
			return;
		}

		boolean singleLine = getSingleLine(nbt);

		builder.print("[");
		builder.print(opening);

		if (singleLine) {
			builder.print(" ");
		} else {
			builder.println();
			builder.push();
		}

		int index = 0;

		for (Tag value : nbt) {
			index++;
			append(builder, value);

			if (index != nbt.size()) {
				builder.print(",");
			}

			if (singleLine) {
				builder.print(" ");
			} else {
				builder.println();
			}
		}

		if (!singleLine) {
			builder.pop();
		}

		builder.print("]");
	}
}