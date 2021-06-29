package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;

import java.util.ArrayList;
import java.util.List;

public class StringValue extends BaseValue<String> {
	private String value;

	StringValue(SNBTConfig c, String n, String def) {
		super(c, n, def);
		value = def;
	}

	public String get() {
		return value;
	}

	@Override
	public void write(SNBTCompoundTag tag) {
		List<String> s = new ArrayList<>(comment);
		s.add("Default: \"" + defaultValue + "\"");
		tag.comment(key, String.join("\n", s));
		tag.putString(key, value);
	}

	@Override
	public void read(SNBTCompoundTag tag) {
		value = tag.getString(key);
	}
}
