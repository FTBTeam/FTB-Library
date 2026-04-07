package dev.ftb.mods.ftblibrary.fabric.integrations.platform;

import dev.ftb.mods.ftblibrary.client.util.PositionedIngredient;
import dev.ftb.mods.ftblibrary.integration.JEIIntegration;
import dev.ftb.mods.ftblibrary.integration.platform.JeiShim;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IJeiRuntime;

import java.util.Optional;

public class JeiShimFabric implements JeiShim {
    @Override
    public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(IJeiRuntime runtime, PositionedIngredient underMouse) {
        if (underMouse.ingredient() instanceof IJeiFluidIngredient stack) {
            Optional<ITypedIngredient<IJeiFluidIngredient>> typed = runtime.getIngredientManager().createTypedIngredient(FabricTypes.FLUID_STACK, stack, false);
            if (typed.isPresent()) {
                return Optional.of(new JEIIntegration.ClickableIngredient<>(typed.get(), underMouse.area()));
            }
        }

        return Optional.empty();
    }
}
