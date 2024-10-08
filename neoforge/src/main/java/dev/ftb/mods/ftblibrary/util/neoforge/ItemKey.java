package dev.ftb.mods.ftblibrary.util.neoforge;

import net.minecraft.world.item.ItemStack;

public final class ItemKey {
    public final ItemStack stack;

    public ItemKey(ItemStack s) {
        stack = s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return ItemStack.isSameItemSameComponents(stack, ((ItemKey) o).stack);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(stack);
    }
}
