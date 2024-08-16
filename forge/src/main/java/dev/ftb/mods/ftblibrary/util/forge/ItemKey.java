package dev.ftb.mods.ftblibrary.util.forge;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;

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
        var itemKey = (ItemKey) o;
        return stack.getItem() == itemKey.stack.getItem() && stack.areShareTagsEqual(itemKey.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack.getItem(), stack.getTag());
    }
}
