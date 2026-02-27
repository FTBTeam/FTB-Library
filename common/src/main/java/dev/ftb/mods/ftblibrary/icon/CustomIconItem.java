package dev.ftb.mods.ftblibrary.icon;

import net.minecraft.world.item.ItemStack;

/**
 * Implement this on items which have a custom icon instead of a default item icon.
 */
public interface CustomIconItem {
    Icon<?> getCustomIcon(ItemStack stack);
}
