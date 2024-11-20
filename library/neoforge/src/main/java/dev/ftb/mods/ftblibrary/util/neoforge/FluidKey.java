package dev.ftb.mods.ftblibrary.util.neoforge;

import net.neoforged.neoforge.fluids.FluidStack;

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
        return FluidStack.isSameFluidSameComponents(stack, fluidKey.stack);
    }

    @Override
    public int hashCode() {
        return FluidStack.hashFluidAndComponents(stack);
    }
}
