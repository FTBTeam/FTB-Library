package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public class DoubleConfig extends NumberConfig<Double> {
	public DoubleConfig(double mn, double mx) {
		super(mn, mx);
	}

	@Override
	public void addInfo(TooltipList list) {
		super.addInfo(list);

		if (min != Double.NEGATIVE_INFINITY) {
			list.add(info("Min", formatValue(min)));
		}

		if (max != Double.POSITIVE_INFINITY) {
			list.add(info("Max", formatValue(max)));
		}
	}

	@Override
	public String getStringFromValue(@Nullable Double v) {
		if (v == null) {
			return "null";
		} else if (v == Double.POSITIVE_INFINITY) {
			return "+Inf";
		} else if (v == Double.NEGATIVE_INFINITY) {
			return "-Inf";
		}

		return super.getStringFromValue(v);
	}

	@Override
	public boolean parse(@Nullable Consumer<Double> callback, String string) {
		if (string.equals("+Inf")) {
			if (max == Double.POSITIVE_INFINITY) {
				if (callback != null) {
					callback.accept(Double.POSITIVE_INFINITY);
				}

				return true;
			}

			return false;
		} else if (string.equals("-Inf")) {
			if (min == Double.NEGATIVE_INFINITY) {
				if (callback != null) {
					callback.accept(Double.NEGATIVE_INFINITY);
				}

				return true;
			}

			return false;
		}

		try {
			var multiplier = 1D;

			if (string.endsWith("K")) {
				multiplier = 1000D;
				string = string.substring(0, string.length() - 1);
			} else if (string.endsWith("M")) {
				multiplier = 1000000D;
				string = string.substring(0, string.length() - 1);
			} else if (string.endsWith("B")) {
				multiplier = 1000000000D;
				string = string.substring(0, string.length() - 1);
			}

			var v = Double.parseDouble(string.trim()) * multiplier;

			if (v >= min && v <= max) {
				if (callback != null) {
					callback.accept(v);
				}

				return true;
			}
		} catch (Exception ex) {
		}

		return false;
	}

	@Override
	public boolean scrollValue(boolean forward) {
		double newVal = Mth.clamp(value + (forward ? 1D : -1D), min, max);
		if (newVal != value) {
			setValue(newVal);
			return true;
		}
		return false;
	}
}