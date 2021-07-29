package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StringValue extends BaseValue<String> {
	private Pattern pattern;

	StringValue(SNBTConfig c, String n, String def) {
		super(c, n, def);
	}

	public StringValue pattern(Pattern p) {
		pattern = p;
		return this;
	}

	@Override
	public void set(String v) {
		super.set(v);

		if (pattern != null && !pattern.matcher(get()).find()) {
			super.set(defaultValue);
		}
	}

	@Override
	public void write(SNBTCompoundTag tag) {
		List<String> s = new ArrayList<>(comment);
		s.add("Default: \"" + defaultValue + "\"");
		tag.comment(key, String.join("\n", s));
		tag.putString(key, get());
	}

	@Override
	public void read(SNBTCompoundTag tag) {
		set(tag.getString(key));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void createClientConfig(ConfigGroup group) {
		group.addString(key, get(), this::set, defaultValue, pattern)
				.setCanEdit(enabled.getAsBoolean())
		;
	}
}
