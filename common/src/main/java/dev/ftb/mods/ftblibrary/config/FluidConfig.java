package dev.ftb.mods.ftblibrary.config;

import dev.architectury.fluid.FluidStack;
import dev.ftb.mods.ftblibrary.config.ui.SelectFluidScreen;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.network.chat.Component;


public class FluidConfig extends ConfigValue<FluidStack> {
	private final boolean allowEmpty;

	public FluidConfig(boolean allowEmpty) {
		this.allowEmpty = allowEmpty;
		defaultValue = FluidStack.empty();
		value = FluidStack.empty();
	}

	public boolean allowEmptyFluid() {
		return allowEmpty;
	}

	@Override
	public Component getStringForGUI(FluidStack v) {
		return v == null ? Component.empty() : v.getName();
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback) {
		if (getCanEdit()) {
			new SelectFluidScreen(this, callback).openGui();
		}
	}
}
