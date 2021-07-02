package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StringValue extends BaseValue<String> {
	private String value;
	private Pattern pattern;

	StringValue(SNBTConfig c, String n, String def) {
		super(c, n, def);
		value = def;
	}

	public StringValue pattern(Pattern p) {
		pattern = p;
		return this;
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

		if (!pattern.matcher(value).find()) {
			value = defaultValue;
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void createClientConfig(ConfigGroup group) {
		group.addString(key, value, v -> value = v, defaultValue, pattern);
	}
}
