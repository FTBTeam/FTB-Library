package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;


public class BooleanConfig extends ConfigWithVariants<Boolean> {
	public static final Component TRUE_TEXT = Component.literal("True");
	public static final Component FALSE_TEXT = Component.literal("False");

	@Override
	public Color4I getColor(@Nullable Boolean v) {
		return v == null || !v ? Tristate.FALSE.color : Tristate.TRUE.color;
	}

	@Override
	public Boolean getIteration(Boolean currentValue, boolean next) {
		return !currentValue;
	}

	@Override
	public Component getStringForGUI(@Nullable Boolean v) {
		return v == null ? NULL_TEXT : v ? TRUE_TEXT : FALSE_TEXT;
	}

	@Override
	public Icon getIcon(@Nullable Boolean v) {
		return v == null || !v ? Icons.ACCEPT_GRAY : Icons.ACCEPT;
	}
}
