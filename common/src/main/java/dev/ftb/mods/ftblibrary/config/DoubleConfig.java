package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class DoubleConfig extends NumberConfig<Double> {
	public DoubleConfig(double mn, double mx) {
		super(mn, mx);
		scrollIncrement = 1.0;
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
		if (string.equals("-") || string.equals("+") || string.isEmpty()) return okValue(callback, 0D);

        switch (string) {
            case "+Inf" -> {
                return max == Double.POSITIVE_INFINITY && okValue(callback, Double.POSITIVE_INFINITY);
            }
            case "-Inf" -> {
				return min == Double.NEGATIVE_INFINITY && okValue(callback, Double.NEGATIVE_INFINITY);
            }
            case "-" -> {
				return min <= 0 && max >= 0 && okValue(callback, 0d);
            }
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
				return okValue(callback, v);
			}
		} catch (Exception ignored) {
		}

		return false;
	}

	@Override
	public Optional<Double> scrollValue(Double currentValue, boolean forward) {
		double newVal = Mth.clamp(currentValue + (forward ? scrollIncrement : -scrollIncrement), min, max);
        return newVal != currentValue ? Optional.of(newVal) : Optional.empty();
    }
}
