package dev.ftb.mods.ftblibrary.config;

import dev.architectury.fluid.FluidStack;
import dev.ftb.mods.ftblibrary.config.ui.SelectFluidScreen;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * @author LatvianModder
 */
public class FluidConfig extends ConfigValue<Fluid> {
	private final boolean allowEmpty;

	public FluidConfig(boolean allowEmpty) {
		this.allowEmpty = allowEmpty;
		defaultValue = Fluids.EMPTY;
		value = Fluids.EMPTY;
	}

	public boolean allowEmptyFluid() {
		return allowEmpty;
	}

	@Override
	public Component getStringForGUI(Fluid v) {
		return FluidStack.create(v, FluidStack.bucketAmount()).getName();
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback) {
		new SelectFluidScreen(this, callback).openGui();
	}
}