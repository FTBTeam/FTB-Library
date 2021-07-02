package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntArrayValue extends BaseValue<int[]> {
	private int[] value;

	IntArrayValue(SNBTConfig c, String n, int[] def) {
		super(c, n, def);
		value = def;
	}

	public int[] get() {
		return value;
	}

	@Override
	public void write(SNBTCompoundTag tag) {
		List<String> s = new ArrayList<>(comment);
		s.add("Default: " + Arrays.toString(defaultValue));
		tag.comment(key, String.join("\n", s));
		tag.putIntArray(key, value);
	}

	@Override
	public void read(SNBTCompoundTag tag) {
		value = tag.getIntArray(key);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void createClientConfig(ConfigGroup group) {
		// group.addList(key, value, new IntConfig(Integer.MIN_VALUE, Integer.MAX_VALUE), 0);
	}
}
