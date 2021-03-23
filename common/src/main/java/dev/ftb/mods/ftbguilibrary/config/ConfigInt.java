package dev.ftb.mods.ftbguilibrary.config;

import dev.ftb.mods.ftbguilibrary.utils.StringUtils;
import dev.ftb.mods.ftbguilibrary.utils.TooltipList;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public class ConfigInt extends ConfigNumber<Integer> {
	public ConfigInt(int mn, int mx) {
		super(mn, mx);
	}

	@Override
	public void addInfo(TooltipList list) {
		super.addInfo(list);

		if (min != Integer.MIN_VALUE) {
			list.add(info("Min", StringUtils.formatDouble(min)));
		}

		if (max != Integer.MAX_VALUE) {
			list.add(info("Max", StringUtils.formatDouble(max)));
		}
	}

	@Override
	public boolean parse(@Nullable Consumer<Integer> callback, String string) {
		try {
			int v = Long.decode(string).intValue();

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