package dev.ftb.mods.ftblibrary.util;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class SetOfItemStack extends ObjectLinkedOpenCustomHashSet<ItemStack> {
    // matches by mod, then by display name
    private static final Comparator<? super ItemStack> COMPARE_STACKS = Comparator
            .comparing((ItemStack stack) -> namespace(stack.getItem()))
            .thenComparing(stack -> stack.getHoverName().getString());

    public SetOfItemStack() {
        super(new ItemStackHashingStrategy());
    }

    public SetOfItemStack(Collection<? extends ItemStack> collection) {
        super(collection, new ItemStackHashingStrategy());
    }

    private static String namespace(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getNamespace();
    }

    public List<ItemStack> sortedList() {
        return this.stream().sorted(COMPARE_STACKS).toList();
    }

    private record ItemStackHashingStrategy() implements Strategy<ItemStack> {
        @Override
        public int hashCode(ItemStack stack) {
            return ItemStack.hashItemAndComponents(stack);
        }

        @Override
        public boolean equals(ItemStack o1, ItemStack o2) {
            return ItemStack.isSameItemSameComponents(o1, o2);
        }
    }
}
