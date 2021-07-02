package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class LongValue extends BaseValue<Long> {
	private long minValue = Long.MIN_VALUE;
	private long maxValue = Long.MAX_VALUE;
	private long value;
	private boolean fader;

	LongValue(SNBTConfig c, String n, long def) {
		super(c, n, def);
		value = def;
	}

	public LongValue range(long min, long max) {
		minValue = min;
		maxValue = max;
		return this;
	}

	public LongValue range(long max) {
		return range(0L, max);
	}

	public LongValue fader() {
		fader = true;
		return this;
	}

	public long get() {
		return value;
	}

	@Override
	public void write(SNBTCompoundTag tag) {
		List<String> c = new ArrayList<>(comment);
		c.add("Default: " + defaultValue);
		c.add("Range: " + (minValue == Long.MIN_VALUE ? "-∞" : minValue) + " ~ " + (maxValue == Long.MAX_VALUE ? "+∞" : maxValue));
		tag.comment(key, String.join("\n", c));
		tag.putLong(key, value);
	}

	@Override
	public void read(SNBTCompoundTag tag) {
		value = Mth.clamp(tag.getLong(key), minValue, maxValue);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void createClientConfig(ConfigGroup group) {
		group.addLong(key, value, v -> value = v, defaultValue, minValue, maxValue);
	}
}
