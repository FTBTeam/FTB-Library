package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

public class LongValue extends NumberValue<Long> {
	LongValue(SNBTConfig c, String n, long def) {
		super(c, n, def);
	}

	public NumberValue<Long> range(long max) {
		return range(0L, max);
	}

	@Override
	public void set(Long v) {
		super.set(Mth.clamp(v, minValue == null ? Long.MIN_VALUE : minValue, maxValue == null ? Long.MAX_VALUE : maxValue));
	}

	@Override
	public void write(SNBTCompoundTag tag) {
		super.write(tag);
		tag.putLong(key, get());
	}

	@Override
	public void read(SNBTCompoundTag tag) {
		set(tag.getLong(key));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void createClientConfig(ConfigGroup group) {
		group.addLong(key, get(), this::set, defaultValue, minValue == null ? Long.MIN_VALUE : minValue, maxValue == null ? Long.MAX_VALUE : maxValue).fader(fader);
	}
}
