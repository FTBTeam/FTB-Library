package dev.ftb.mods.ftblibrary.config.ui.fabric;

import dev.architectury.fluid.FluidStack;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class SelectFluidScreenImpl {
	public static ResourceLocation getStillTexture(FluidStack stack) {
		var fluid = stack.getFluid();
		var handler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);

		if (handler == null) {
			return null;
		}

		var sprites = handler.getFluidSprites(Minecraft.getInstance().level, Minecraft.getInstance().level == null ? null : BlockPos.ZERO, fluid.defaultFluidState());
		if (sprites[0] == null) {
			return null;
		}
		return sprites[0].getName();
	}

	public static int getColor(FluidStack stack) {
		var fluid = stack.getFluid();
		var handler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);

		var color = -1;
		if (handler != null) {
			if (Minecraft.getInstance().level != null) {
				color = handler.getFluidColor(Minecraft.getInstance().level, BlockPos.ZERO, fluid.defaultFluidState());
			} else {
				color = handler.getFluidColor(null, null, fluid.defaultFluidState());
			}
		}

		return color;
	}
}
