package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class IntConfig extends NumberConfig<Integer> {
	public IntConfig(int mn, int mx) {
		super(mn, mx);
		scrollIncrement = 1;
	}

	@Override
	public void addInfo(TooltipList list) {
		super.addInfo(list);

		if (min != Integer.MIN_VALUE) {
			list.add(info("Min", formatValue(min)));
		}

		if (max != Integer.MAX_VALUE) {
			list.add(info("Max", formatValue(max)));
		}
	}

	@Override
	public boolean parse(@Nullable Consumer<Integer> callback, String string) {
		try {
			var v = Long.decode(string).intValue();

			if (v >= min && v <= max) {
				if (callback != null) {
					callback.accept(v);
				}

				return true;
			}
		} catch (Exception ignored) {
		}

		return false;
	}

	@Override
	protected String formatValue(Integer v) {
		return String.format("%,d", v);
	}

	@Override
	public Optional<Integer> scrollValue(Integer currentValue, boolean forward) {
		int newVal = Mth.clamp(currentValue + (forward ? scrollIncrement : -scrollIncrement), min, max);
        return newVal != currentValue ? Optional.of(newVal) : Optional.empty();
    }
}
