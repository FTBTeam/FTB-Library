package dev.ftb.mods.ftbguilibrary.config;

import dev.ftb.mods.ftbguilibrary.icon.Color4I;
import dev.ftb.mods.ftbguilibrary.utils.StringUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.Nullable;

/**
 * @author LatvianModder
 */
public abstract class ConfigNumber<T extends Number> extends ConfigFromString<T> {
	public static final Color4I COLOR = Color4I.rgb(0xAA5AE8);

	public final T min;
	public final T max;

	public ConfigNumber(T mn, T mx) {
		min = mn;
		max = mx;
	}

	@Override
	public Color4I getColor(@Nullable T v) {
		return COLOR;
	}

	@Override
	public Component getStringForGUI(@Nullable T v) {
		return v == null ? NULL_TEXT : new TextComponent(StringUtils.formatDouble(v.doubleValue(), true));
	}
}