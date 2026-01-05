package dev.ftb.mods.ftblibrary.config.ui.resource;

import dev.architectury.fluid.FluidStack;
import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Wraps a resource which can be searched for in a search GUI, e.g. items, fluids, images, entity faces...
 *
 * @param <T> the resource type
 */
public interface SelectableResource<T> {
    static SelectableResource<ItemStack> item(ItemStack stack) {
        return new ItemStackResource(stack);
    }

    static SelectableResource<FluidStack> fluid(FluidStack stack) {
        return new FluidStackResource(stack);
    }

    /**
     * {@return the wrapped resource, being selected}
     */
    T resource();

    /**
     * {@return the resource count; this may be hardwired for 1 for some resource types, e.g. images}
     */
    long getCount();

    /**
     * Set the resource count. This may be ignored for some resource types.
     * @param count the new amount
     */
    void setCount(int count);

    /**
     * {@return true if this is an empty instance}
     */
    default boolean isEmpty() {
        return getCount() == 0;
    }

    /**
     * {@return the displayable name for this resource}
     */
    Component getName();

    /**
     * {@return the displayable icon for this resource}
     */
    Icon getIcon();

    /**
     * Deep-copy this resource, with an associated count.
     * @param count the amount of the copied resource
     * @return a deep-copy of this resource
     */
    SelectableResource<T> copyWithCount(long count);

    /**
     * {@return any component data, serialised into an NBT compound tag, or null if no extended data or not applicable}
     */
    @Nullable
    default CompoundTag getComponentsTag() {
        return null;
    }

    /**
     * Apply component data to this resource. This may be a no-op if not applicable for this resource type.
     *
     * @param tag the data to apply
     */
    default void applyComponentsTag(CompoundTag tag) {
    }
}
