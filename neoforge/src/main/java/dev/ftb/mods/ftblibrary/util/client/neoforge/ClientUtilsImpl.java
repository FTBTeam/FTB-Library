package dev.ftb.mods.ftblibrary.util.client.neoforge;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.FluidStackHooks;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class ClientUtilsImpl {
    public static ResourceLocation getStillTexture(FluidStack stack) {
        var neoforgeStack = new net.neoforged.neoforge.fluids.FluidStack(stack.getFluid(), (int) stack.getAmount(), stack.getTag());
        TextureAtlasSprite stillTexture = FluidStackHooks.getStillTexture(neoforgeStack.getFluid());
        if (stillTexture == null) return null;
        // don't use try-with-resources here, it will cause a "image not allocated" crash
        return stillTexture.contents().name();
    }

    public static int getFluidColor(FluidStack stack) {
        var forgeStack = new net.neoforged.neoforge.fluids.FluidStack(stack.getFluid(), (int) stack.getAmount(), stack.getTag());

        return FluidStackHooks.getColor(forgeStack.getFluid());
    }
}
