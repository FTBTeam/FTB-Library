package dev.ftb.mods.ftbguilibrary.config;

import dev.ftb.mods.ftbguilibrary.utils.StringUtils;
import dev.ftb.mods.ftbguilibrary.utils.TooltipList;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public class LongConfig extends NumberConfig<Long> {
	public LongConfig(long mn, long mx) {
		super(mn, mx);
	}

	@Override
	public void addInfo(TooltipList list) {
		super.addInfo(list);

		if (min != Long.MIN_VALUE) {
			list.add(info("Min", StringUtils.formatDouble(min)));
		}

		if (max != Long.MAX_VALUE) {
			list.add(info("Max", StringUtils.formatDouble(max)));
		}
	}

	@Override
	public boolean parse(@Nullable Consumer<Long> callback, String string) {
		try {
			long v = Long.decode(string);

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
}