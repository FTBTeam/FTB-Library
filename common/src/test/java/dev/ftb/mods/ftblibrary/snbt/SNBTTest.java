package dev.ftb.mods.ftblibrary.snbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SNBTTest {
	public static void main(String[] args) {
		OrderedCompoundTag tag = new OrderedCompoundTag();
		tag.comment("hi", "Hello (Boolean test)");
		tag.putBoolean("hi", false);

		ListTag messages = new ListTag();

		for (int i = 0; i < 3; i++) {
			OrderedCompoundTag message = new OrderedCompoundTag();
			message.singleLine();
			message.putString("sender", "LatvianModder");
			message.putString("content", "Hello\nMy name is\tLat! Here's a slash: \\");
			message.putLong("date", System.currentTimeMillis());
			messages.add(message);
		}

		tag.comment("messages", "Message list", "", "This comment is in multiple lines!", "");
		tag.put("messages", messages);

		tag.comment("test_int", "Just an integer");
		tag.putInt("test_int", 30);

		System.out.println();
		System.out.println();

		List<String> lines = SNBT.writeLines(tag);

		for (String s : lines) {
			System.out.println(s);
		}

		System.out.println();
		System.out.println();

		CompoundTag reverseTag = SNBT.readLines(lines);

		System.out.println("Reverse tag test: " + (reverseTag != null ? "success" : "fail"));
		System.out.println();
		System.out.println();

		try (InputStream stream = SNBTTest.class.getResourceAsStream("/snbt_test.snbt")) {
			CompoundTag newParserTest = SNBT.readLines(new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines().collect(Collectors.toList()));

			System.out.println("New parser test: ");
			System.out.println(newParserTest);

			System.out.println();
			System.out.println();

			List<String> lines2 = SNBT.writeLines(newParserTest);

			for (String s : lines2) {
				System.out.println(s);
			}

			CompoundTag newParserTest2 = SNBT.readLines(lines2);

			System.out.println();
			System.out.println();
			System.out.println("New parser [1]: " + newParserTest);
			System.out.println("New parser [2]: " + newParserTest2);
			System.out.println("New parser reverse tag test: " + (Objects.equals(newParserTest, newParserTest2) ? "success" : "fail"));
		} catch (SNBTSyntaxException ex) {
			System.out.println("New parser test: failed: " + ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("New parser test: failed: " + ex);
		}
	}
}
