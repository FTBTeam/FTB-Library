package dev.ftb.mods.ftblibrary.api.sidebar;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public interface SidebarButton {

    /**
     * @return the id of the button used for saving config data created from the location button in resource path
     */
    ResourceLocation getId();

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
