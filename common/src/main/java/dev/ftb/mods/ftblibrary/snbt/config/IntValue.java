package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

public class IntValue extends NumberValue<Integer> {
	IntValue(SNBTConfig c, String n, int def) {
		super(c, n, def);
	}

	public NumberValue<Integer> range(int max) {
		return range(0, max);
	}

	@Override
	public void set(Integer v) {
		super.set(Mth.clamp(v, minValue == null ? Integer.MIN_VALUE : minValue, maxValue == null ? Integer.MAX_VALUE : maxValue));
	}

	@Override
	public void write(SNBTCompoundTag tag) {
		super.write(tag);
		tag.putInt(key, get());
	}

	@Override
	public void read(SNBTCompoundTag tag) {
		set(tag.getInt(key));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void createClientConfig(ConfigGroup group) {
		group.addInt(key, get(), this::set, defaultValue, minValue == null ? Integer.MIN_VALUE : minValue, maxValue == null ? Integer.MAX_VALUE : maxValue).fader(fader);
	}
}
