package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public abstract class NumberConfig<T extends Number> extends ConfigFromString<T> {
	public static final Color4I COLOR = Color4I.rgb(0xAA5AE8);

	public final T min;
	public final T max;
	public boolean fader;

	public NumberConfig(T mn, T mx) {
		min = mn;
		max = mx;
	}

	@Override
	public Color4I getColor(@Nullable T v) {
		return COLOR;
	}

	public NumberConfig<T> fader(boolean v) {
		fader = v;
		return this;
	}

	@Override
	public boolean canScroll() {
		return true;
	}

	@Override
	public Component getStringForGUI(@Nullable T v) {
		return v == null ? NULL_TEXT : Component.literal(formatValue(v));
	}

	protected String formatValue(T v) {
		return StringUtils.formatDouble(v.doubleValue(), true);
	}
}
