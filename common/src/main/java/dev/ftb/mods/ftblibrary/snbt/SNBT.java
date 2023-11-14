package dev.ftb.mods.ftblibrary.snbt;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;


public class SNBT {
	private static boolean shouldSortKeysOnWrite = false;

	public static SNBTCompoundTag readLines(List<String> lines) {
		return SNBTParser.read(lines);
	}

	public static SNBTCompoundTag tryRead(Path path) throws IOException {
		return readLines(Files.readAllLines(path, StandardCharsets.UTF_8));
	}

	public static void tryWrite(Path path, CompoundTag tag) throws IOException {
		if (Files.notExists(path.getParent())) {
			Files.createDirectories(path.getParent());
		}

		Files.write(path, writeLines(tag));
	}

	@Nullable
	public static SNBTCompoundTag read(Path path) {
		if (Files.notExists(path) || Files.isDirectory(path) || !Files.isReadable(path)) {
			return null;
		}

		try {
			return readLines(Files.readAllLines(path, StandardCharsets.UTF_8));
		} catch (SNBTSyntaxException ex) {
			FTBLibrary.LOGGER.error("Failed to read " + path + ": " + ex.getMessage());
			return null;
		} catch (Exception ex) {
			FTBLibrary.LOGGER.error("Failed to read " + path + ": " + ex);
			ex.printStackTrace();
			return null;
		}
	}

	public static List<String> writeLines(CompoundTag nbt) {
		var builder = new SNBTBuilder();

		var rootProperties = nbt instanceof SNBTCompoundTag ? ((SNBTCompoundTag) nbt).getProperties("") : SNBTTagProperties.DEFAULT;

		if (!rootProperties.comment.isEmpty()) {
			for (var s : rootProperties.comment.split("\n")) {
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
		} else if (nbt instanceof CompoundTag compound) {
			var snbtCompoundTag = compound instanceof SNBTCompoundTag s ? s : null;

			if (compound.isEmpty()) {
				builder.print("{ }");
				return;
			}

			if (snbtCompoundTag != null && snbtCompoundTag.singleLine) {
				builder.singleLine++;
			}

			var singleLine = builder.singleLine > 0;

			builder.print("{");

			if (singleLine) {
				builder.print(" ");
			} else {
				builder.println();
				builder.push();
			}

			var index = 0;

			Collection<String> keys = shouldSortKeysOnWrite ? compound.getAllKeys().stream().sorted().toList() : compound.getAllKeys();
			for (var key : keys) {
				index++;
				var properties = snbtCompoundTag == null ? SNBTTagProperties.DEFAULT : snbtCompoundTag.getProperties(key);

				if (!properties.comment.isEmpty()) {
					if (singleLine) {
						throw new IllegalStateException("Can't have singleLine enabled and a comment at the same time!");
					}

					if (index != 1) {
						builder.println();
					}

					for (var s : properties.comment.split("\n")) {
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

			if (snbtCompoundTag != null && snbtCompoundTag.singleLine) {
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

		var singleLine = builder.singleLine > 0;

		builder.print("[");
		builder.print(opening);

		if (singleLine) {
			builder.print(" ");
		} else {
			builder.println();
			builder.push();
		}

		var index = 0;

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

	public static boolean shouldSortKeysOnWrite() {
		return shouldSortKeysOnWrite;
	}

	public static boolean setShouldSortKeysOnWrite(boolean shouldSortKeysOnWrite) {
		boolean prev = SNBT.shouldSortKeysOnWrite;
		SNBT.shouldSortKeysOnWrite = shouldSortKeysOnWrite;
		return prev;
	}
}
