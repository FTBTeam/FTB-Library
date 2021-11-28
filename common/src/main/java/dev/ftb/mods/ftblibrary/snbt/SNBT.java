package dev.ftb.mods.ftblibrary.snbt;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.architectury.platform.Platform;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author LatvianModder
 */
public class SNBT {
	public static SNBTCompoundTag readLines(List<String> lines) {
		return SNBTParser.read(lines);
	}

	@Nullable
	public static SNBTCompoundTag read(Path path) {
		if (Files.notExists(path) || Files.isDirectory(path) || !Files.isReadable(path)) {
			return null;
		}

		try {
			return readLines(Files.readAllLines(path, StandardCharsets.UTF_8));
		} catch (SNBTSyntaxException ex) {
			FTBLibrary.LOGGER.error("Failed to read " + Platform.getGameFolder().relativize(path) + ": " + ex.getMessage());
			return null;
		} catch (Exception ex) {
			FTBLibrary.LOGGER.error("Failed to read " + Platform.getGameFolder().relativize(path) + ": " + ex);
			ex.printStackTrace();
			return null;
		}
	}

	public static List<String> writeLines(CompoundTag nbt) {
		SNBTBuilder builder = new SNBTBuilder();

		SNBTTagProperties rootProperties = nbt instanceof SNBTCompoundTag ? ((SNBTCompoundTag) nbt).getProperties("") : SNBTTagProperties.DEFAULT;

		if (!rootProperties.comment.isEmpty()) {
			for (String s : rootProperties.comment.split("\n")) {
				builder.print("# ");
				builder.print(s);
				builder.println();
			}

			builder.println();
		}

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
			SNBTCompoundTag ocompound = compound instanceof SNBTCompoundTag ? (SNBTCompoundTag) compound : null;

			if (compound.isEmpty()) {
				builder.print("{ }");
				return;
			}

			if (ocompound != null && ocompound.singleLine) {
				builder.singleLine++;
			}

			boolean singleLine = builder.singleLine > 0;

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
				SNBTTagProperties properties = ocompound == null ? SNBTTagProperties.DEFAULT : ocompound.getProperties(key);

				if (!properties.comment.isEmpty()) {
					if (singleLine) {
						throw new IllegalStateException("Can't have singleLine enabled and a comment at the same time!");
					}

					if (index != 1) {
						builder.println();
					}

					for (String s : properties.comment.split("\n")) {
						builder.print("# ");
						builder.print(s);
						builder.println();
					}
				}

				builder.print(SNBTUtils.handleEscape(key));
				builder.print(": ");

				if (properties.valueType == SNBTTagProperties.TYPE_FALSE) {
					builder.print("false");
				} else if (properties.valueType == SNBTTagProperties.TYPE_TRUE) {
					builder.print("true");
				} else {
					if (properties.singleLine) {
						builder.singleLine++;
					}

					append(builder, compound.get(key));

					if (properties.singleLine) {
						builder.singleLine--;
					}
				}

				if (singleLine && index != compound.size()) {
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

			if (ocompound != null && ocompound.singleLine) {
				builder.singleLine--;
			}
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
		} else if (nbt instanceof StringTag) {
			builder.print(SNBTUtils.quoteAndEscape(nbt.getAsString()));
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

		boolean singleLine = builder.singleLine > 0;

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

			if (singleLine && index != nbt.size()) {
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