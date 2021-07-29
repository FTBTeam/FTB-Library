package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;

public class BooleanValue extends BaseValue<Boolean> {
	BooleanValue(SNBTConfig c, String n, boolean def) {
		super(c, n, def);
	}

	public void toggle() {
		set(!get());
	}

	@Override
	public void write(SNBTCompoundTag tag) {
		List<String> s = new ArrayList<>(comment);
		s.add("Default: " + defaultValue);
		tag.comment(key, String.join("\n", s));
		tag.putBoolean(key, get());
	}

	@Override
	public void read(SNBTCompoundTag tag) {
		set(tag.getBoolean(key));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void createClientConfig(ConfigGroup group) {
		group.addBool(key, get(), this::set, defaultValue)
				.setCanEdit(enabled.getAsBoolean())
		;
	}
}
