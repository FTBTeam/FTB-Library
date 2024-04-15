package dev.ftb.mods.ftblibrary.config;

import dev.architectury.fluid.FluidStack;
import dev.ftb.mods.ftblibrary.config.ui.SelectFluidScreen;
import dev.ftb.mods.ftblibrary.config.ui.SelectableResource;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.network.chat.Component;

import java.util.OptionalLong;

public class FluidConfig extends ResourceConfigValue<FluidStack> {
	private final boolean allowEmpty;
	private final boolean isFixedSize;
	private final long fixedSize;
	private boolean showAmount = true;

	public FluidConfig(boolean allowEmpty) {
		this.isFixedSize = false;
		this.fixedSize = 0;
		this.allowEmpty = allowEmpty;
		defaultValue = FluidStack.empty();
		value = FluidStack.empty();
	}

	public FluidConfig(long fixedSize) {
		this.isFixedSize = true;
		this.fixedSize = fixedSize;
		this.allowEmpty = false;
		defaultValue = FluidStack.empty();
		value = FluidStack.empty();
	}

	public FluidConfig showAmount(boolean show) {
		showAmount = show;
		return this;
	}

	@Override
	public Component getStringForGUI(FluidStack v) {
        if (v == null || v.isEmpty()) {
			return Component.translatable("gui.none");
		}
        return showAmount ? Component.literal(v.getAmount() + "mB ").append(v.getName()) : v.getName();
    }

	@Override
	public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
		if (getCanEdit()) {
			new SelectFluidScreen(this, callback).openGui();
		}
	}

	@Override
	public boolean allowEmptyResource() {
		return allowEmpty;
	}

	@Override
	public OptionalLong fixedResourceSize() {
		return isFixedSize ? OptionalLong.of(fixedSize) : OptionalLong.empty();
	}

	@Override
	public boolean isEmpty() {
		return getValue().isEmpty();
	}

	@Override
	public SelectableResource<FluidStack> getResource() {
		return SelectableResource.fluid(getValue());
	}

	@Override
	public boolean setResource(SelectableResource<FluidStack> selectedStack) {
		return setCurrentValue(selectedStack.stack());
	}
}