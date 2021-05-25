package dev.ftb.mods.ftblibrary.snbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
			message.putString("content", "Hello");
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

		try (InputStream stream = SNBTTest.class.getResourceAsStream("/snbt_test.snbt")) {
			CompoundTag newParserTest = SNBTParser.read(new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)));

			System.out.println("New parser test: " + newParserTest);

			System.out.println();
			System.out.println();

			List<String> lines2 = SNBT.writeLines(newParserTest);

			for (String s : lines2) {
				System.out.println(s);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("New parser test: failed");
		}
	}
}
