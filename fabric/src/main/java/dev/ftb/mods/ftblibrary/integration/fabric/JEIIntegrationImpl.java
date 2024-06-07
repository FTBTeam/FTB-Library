package dev.ftb.mods.ftblibrary.integration.fabric;


import dev.architectury.fluid.FluidStack;
import dev.ftb.mods.ftblibrary.integration.JEIIntegration;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IJeiRuntime;

import java.util.Optional;

public class JEIIntegrationImpl {
    public static Optional<IClickableIngredient<?>> handleExtraIngredientTypes(IJeiRuntime runtime, PositionedIngredient underMouse) {
        if (underMouse.ingredient() instanceof FluidStack stack) {
            if (runtime.getJeiHelpers().getPlatformFluidHelper().create(stack.getFluid(), stack.getAmount()) instanceof IJeiFluidIngredient ingr) {
                Optional<ITypedIngredient<IJeiFluidIngredient>> typed = runtime.getIngredientManager().createTypedIngredient(FabricTypes.FLUID_STACK, ingr);
                if (typed.isPresent()) {
                    return Optional.of(new JEIIntegration.ClickableIngredient<>(typed.get(), underMouse.area()));
                }
            }
        }

        return Optional.empty();
    }
}
