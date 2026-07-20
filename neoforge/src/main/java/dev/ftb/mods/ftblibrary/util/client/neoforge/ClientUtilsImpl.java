package dev.ftb.mods.ftblibrary.util.client.neoforge;

import dev.ftb.mods.ftblibrary.util.input.Input;
import dev.ftb.mods.ftblibrary.util.neoforge.InputImpl;
import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.FluidStackHooks;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

public class ClientUtilsImpl {
    private static final Input neoInputImpl = new InputImpl();

    public static Input input() {
        return neoInputImpl;
    }

    public static ResourceLocation getStillTexture(FluidStack stack) {
        var neoforgeStack = new net.neoforged.neoforge.fluids.FluidStack(Holder.direct(stack.getFluid()), (int) stack.getAmount(), stack.getPatch());
        TextureAtlasSprite stillTexture = FluidStackHooks.getStillTexture(neoforgeStack.getFluid());
        if (stillTexture == null) return null;
        // don't use try-with-resources here, it will cause a "image not allocated" crash
        return stillTexture.contents().name();
    }

    public static int getFluidColor(FluidStack stack) {
        var forgeStack = new net.neoforged.neoforge.fluids.FluidStack(Holder.direct(stack.getFluid()), (int) stack.getAmount(), stack.getPatch());

        return FluidStackHooks.getColor(forgeStack.getFluid());
    }
}
