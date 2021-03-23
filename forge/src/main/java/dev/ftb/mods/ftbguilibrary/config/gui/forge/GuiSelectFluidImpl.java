package dev.ftb.mods.ftbguilibrary.config.gui.forge;

import me.shedaniel.architectury.fluid.FluidStack;
import net.minecraft.resources.ResourceLocation;

public class GuiSelectFluidImpl {
	public static ResourceLocation getStillTexture(FluidStack stack) {
		net.minecraftforge.fluids.FluidStack forgeStack = new net.minecraftforge.fluids.FluidStack(stack.getFluid(), stack.getAmount().intValue(), stack.getTag());

		return forgeStack.getFluid().getAttributes().getStillTexture(forgeStack);
	}

	public static int getColor(FluidStack stack) {
		net.minecraftforge.fluids.FluidStack forgeStack = new net.minecraftforge.fluids.FluidStack(stack.getFluid(), stack.getAmount().intValue(), stack.getTag());

		return forgeStack.getFluid().getAttributes().getColor(forgeStack);
	}
}
