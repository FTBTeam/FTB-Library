package dev.ftb.mods.ftblibrary.snbt;

import net.minecraft.nbt.ListTag;

public class SNBTTest {
	public static void main(String[] args) {
		OrderedCompoundTag tag = new OrderedCompoundTag();
		tag.putBoolean("hi", false);

		ListTag messages = new ListTag();

		for (int i = 0; i < 5; i++) {
			OrderedCompoundTag message = new OrderedCompoundTag();
			message.singleLine = true;
			message.putString("sender", "LatvianModder");
			message.putString("content", "Hello");
			message.putLong("date", System.currentTimeMillis());
			messages.add(message);
		}

		tag.put("messages", messages);

		tag.putInt("test_int", 30);

		for (String s : SNBT.writeLines(tag)) {
			System.out.println(s);
		}
	}
}
