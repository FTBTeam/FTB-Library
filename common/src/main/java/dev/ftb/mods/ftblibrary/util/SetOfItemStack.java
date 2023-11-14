package dev.ftb.mods.ftblibrary.util;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class SetOfItemStack extends ObjectLinkedOpenCustomHashSet<ItemStack> {
    private record ItemStackHashingStrategy() implements Strategy<ItemStack> {
        @Override
        public int hashCode(ItemStack stack) {
            return Objects.hash(Item.getId(stack.getItem()), stack.hasTag() ? stack.getTag().hashCode() : 0);
        }

        @Override
        public boolean equals(ItemStack o1, ItemStack o2) {
            return (o1 == o2) || !(o1 == null || o2 == null)
                    && o1.getItem() == o2.getItem()
                    && tagsMatch(o1.getTag(), o2.getTag());
        }

        private boolean tagsMatch(CompoundTag tag1, CompoundTag tag2) {
            return (tag1 != null || tag2 == null)
                    && (tag1 == null || tag2 != null)
                    && (tag1 == null || tag1.equals(tag2));
        }
    }

    public SetOfItemStack() {
        super(new ItemStackHashingStrategy());
    }

    public SetOfItemStack(Collection<? extends ItemStack> collection) {
        super(collection, new ItemStackHashingStrategy());
    }

    public List<ItemStack> sortedList() {
        return this.stream().sorted(COMPARE_STACKS).toList();
    }

    // matches by mod, then by display name
    private static final Comparator<? super ItemStack> COMPARE_STACKS = Comparator
            .comparing((ItemStack stack) -> namespace(stack.getItem()))
            .thenComparing(stack -> stack.getHoverName().getString());

    private static String namespace(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getNamespace();
    }
}
