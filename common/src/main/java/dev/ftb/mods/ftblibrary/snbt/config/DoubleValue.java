package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class DoubleValue extends BaseValue<Double> {
	private double minValue = Double.NEGATIVE_INFINITY;
	private double maxValue = Double.POSITIVE_INFINITY;
	private double value;

	DoubleValue(SNBTConfig c, String n, double def) {
		super(c, n, def);
		value = def;
	}

	public DoubleValue range(double min, double max) {
		minValue = min;
		maxValue = max;
		return this;
	}

	public DoubleValue range(double max) {
		return range(0D, max);
	}

	public double get() {
		return value;
	}

	@Override
	public void write(SNBTCompoundTag tag) {
		List<String> c = new ArrayList<>(comment);
		c.add("Default: " + defaultValue + "d");
		c.add("Range: " + (minValue == Double.NEGATIVE_INFINITY ? "-∞" : (minValue + "d")) + " ~ " + (maxValue == Double.POSITIVE_INFINITY ? "+∞" : (maxValue + "d")));
		tag.comment(key, String.join("\n", c));
		tag.putDouble(key, value);
	}

	@Override
	public void read(SNBTCompoundTag tag) {
		value = Mth.clamp(tag.getDouble(key), minValue, maxValue);
	}
}
