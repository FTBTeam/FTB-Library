package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;

public class BooleanValue extends BaseValue<Boolean> {
	private boolean value;

	BooleanValue(SNBTConfig c, String n, boolean def) {
		super(c, n, def);
		value = def;
	}

	public boolean get() {
		return value;
	}

	@Override
	public void write(SNBTCompoundTag tag) {
		List<String> s = new ArrayList<>(comment);
		s.add("Default: " + defaultValue);
		tag.comment(key, String.join("\n", s));
		tag.putBoolean(key, value);
	}

	@Override
	public void read(SNBTCompoundTag tag) {
		value = tag.getBoolean(key);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void createClientConfig(ConfigGroup group) {
		group.addBool(key, value, v -> value = v, defaultValue);
	}
}
