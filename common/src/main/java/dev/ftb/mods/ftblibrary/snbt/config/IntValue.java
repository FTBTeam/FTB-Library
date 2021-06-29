package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class IntValue extends BaseValue<Integer> {
	private int minValue = Integer.MIN_VALUE;
	private int maxValue = Integer.MAX_VALUE;
	private int value;

	IntValue(SNBTConfig c, String n, int def) {
		super(c, n, def);
		value = def;
	}

	public IntValue range(int min, int max) {
		minValue = min;
		maxValue = max;
		return this;
	}

	public IntValue range(int max) {
		return range(0, max);
	}

	public int get() {
		return value;
	}

	@Override
	public void write(SNBTCompoundTag tag) {
		List<String> c = new ArrayList<>(comment);
		c.add("Default: " + defaultValue);
		c.add("Range: " + (minValue == Integer.MIN_VALUE ? "-∞" : minValue) + " ~ " + (maxValue == Integer.MAX_VALUE ? "+∞" : maxValue));
		tag.comment(key, String.join("\n", c));
		tag.putInt(key, value);
	}

	@Override
	public void read(SNBTCompoundTag tag) {
		value = Mth.clamp(tag.getInt(key), minValue, maxValue);
	}
}
