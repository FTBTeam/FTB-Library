package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.math.MathUtils;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class LongConfig extends NumberConfig<Long> {
	public LongConfig(long mn, long mx) {
		super(mn, mx);
	}

	@Override
	public void addInfo(TooltipList list) {
		super.addInfo(list);

		if (min != Long.MIN_VALUE) {
			list.add(info("Min", formatValue(min)));
		}

		if (max != Long.MAX_VALUE) {
			list.add(info("Max", formatValue(max)));
		}
	}

	@Override
	public boolean parse(@Nullable Consumer<Long> callback, String string) {
		if (string.equals("-") || string.equals("+") || string.isEmpty()) return okValue(callback, 0L);

		try {
			long v = Long.decode(string);
			if (v >= min && v <= max) {
				return okValue(callback, v);
			}
		} catch (Exception ignored) {
		}

		return false;
	}

	@Override
	protected String formatValue(Long v) {
		return String.format("%,d", v);
	}

	@Override
	public Optional<Long> scrollValue(Long currentValue, boolean forward) {
		long newVal = MathUtils.clamp(currentValue + (forward ? 1L : -1L), min, max);
		return newVal != currentValue ? Optional.of(newVal) : Optional.empty();
	}
}
