package dev.ftb.mods.ftblibrary.util.client.neoforge;

import dev.architectury.fluid.FluidStack;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public class ClientUtilsImpl {
    public static Identifier getStillTexture(FluidStack stack) {
        var neoforgeStack = IClientFluidTypeExtensions.of(stack.getFluid());
        return neoforgeStack.getStillTexture(new net.neoforged.neoforge.fluids.FluidStack(stack.getFluid(), (int) stack.getAmount(), stack.getPatch()));
    }

    public static int getFluidColor(FluidStack stack) {
        var forgeStack = new net.neoforged.neoforge.fluids.FluidStack(Holder.direct(stack.getFluid()), (int) stack.getAmount(), stack.getPatch());

        return IClientFluidTypeExtensions.of(stack.getFluid()).getTintColor(forgeStack);
    }
}
