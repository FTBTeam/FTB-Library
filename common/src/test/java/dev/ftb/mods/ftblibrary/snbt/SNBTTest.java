package dev.ftb.mods.ftblibrary.snbt;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SNBTTest {
	public static void main(String[] args) {
		SNBTCompoundTag tag = new SNBTCompoundTag();
		tag.comment("hi", "Hello (Boolean test)");
		tag.putBoolean("hi", false);

		ListTag messages = new ListTag();

		for (int i = 0; i < 3; i++) {
			SNBTCompoundTag message = new SNBTCompoundTag();
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
		tag.putNull("test_null");

		List<String> lines = SNBT.writeLines(tag);
		CompoundTag reverseTag = SNBT.readLines(lines);

		System.out.println("Reverse tag test: " + (reverseTag != null ? "success" : "fail"));

		try (InputStream stream = SNBTTest.class.getResourceAsStream("/snbt_test.snbt")) {
			CompoundTag newParserTest = SNBT.readLines(new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines().collect(Collectors.toList()));

			List<String> lines2 = SNBT.writeLines(newParserTest);
			CompoundTag newParserTest2 = SNBT.readLines(lines2);

			System.out.println("New parser reverse tag test: " + (Objects.equals(newParserTest, newParserTest2) ? "success" : "fail"));
		} catch (SNBTSyntaxException ex) {
			System.out.println("New parser test: failed: " + ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("New parser test: failed: " + ex);
		}

		FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
		SNBTNet.write(byteBuf, tag);
		byteBuf.setIndex(0, byteBuf.capacity());
		SNBTCompoundTag netTag = SNBTNet.readCompound(byteBuf);

		System.out.println("Network IO test: " + (tag.equals(netTag) ? "success" : "fail"));
	}
}
