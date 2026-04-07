package dev.ftb.mods.ftblibrary.integration.platform;

import dev.ftb.mods.ftblibrary.client.util.PositionedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IJeiRuntime;

import java.util.Optional;

public interface JeiShim {
    Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(IJeiRuntime runtime, PositionedIngredient underMouse);
}
