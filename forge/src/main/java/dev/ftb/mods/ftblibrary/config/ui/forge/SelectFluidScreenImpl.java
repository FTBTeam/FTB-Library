package dev.ftb.mods.ftblibrary.config.ui.forge;

import dev.architectury.fluid.FluidStack;
import net.minecraft.resources.ResourceLocation;

public class SelectFluidScreenImpl {
	public static ResourceLocation getStillTexture(FluidStack stack) {
		net.minecraftforge.fluids.FluidStack forgeStack = new net.minecraftforge.fluids.FluidStack(stack.getFluid(), stack.getAmount().intValue(), stack.getTag());

		return forgeStack.getFluid().getAttributes().getStillTexture(forgeStack);
	}

	public static int getColor(FluidStack stack) {
		net.minecraftforge.fluids.FluidStack forgeStack = new net.minecraftforge.fluids.FluidStack(stack.getFluid(), stack.getAmount().intValue(), stack.getTag());

		return forgeStack.getFluid().getAttributes().getColor(forgeStack);
	}
}
