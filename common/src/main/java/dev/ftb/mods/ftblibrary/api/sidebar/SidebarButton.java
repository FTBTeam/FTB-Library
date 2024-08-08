package dev.ftb.mods.ftblibrary.api.sidebar;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public interface SidebarButton {

    /**
     * @return id of button used for storing
     */
    ResourceLocation getId();

    /**
     * @param customTextHandler A supplier that returns the text to be displayed on the button
     * @deprecated Use {@link #addOverlayRender(ButtonOverlayRender)} instead
     */
    @Deprecated(forRemoval = true)
    default void setCustomTextHandler(Supplier<String> customTextHandler) {
        addOverlayRender(ButtonOverlayRender.ofSimpleString(customTextHandler));
    }

    /**
     * Register a condition that must be met for the button to be visible
     * @param condition a condition that must be met for the button to be visible
     */
    void addVisibilityCondition(BooleanSupplier condition);

    /**
     * Register a custom overlay renderer to render on top of the button icon
     * @param renderer the renderer to render on top of the button icon
     */
    void addOverlayRender(ButtonOverlayRender renderer);

    /**
     * Override the default tooltip displayed when hovering over the button
     * @param tooltipOverride a supplier that returns the tooltip to be displayed when hovering over the button
     */
    void setTooltipOverride(Supplier<List<Component>> tooltipOverride);

}
