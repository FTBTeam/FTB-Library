package dev.ftb.mods.ftblibrary.util.forge;

import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Objects;

public final class FluidKey {
	public final FluidStack stack;

	public FluidKey(FluidStack s) {
		stack = s;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		var fluidKey = (FluidKey) o;
		return stack.isFluidEqual(fluidKey.stack);
	}

	@Override
	public int hashCode() {
		return Objects.hash(stack.getFluid(), stack.getTag());
	}
}
