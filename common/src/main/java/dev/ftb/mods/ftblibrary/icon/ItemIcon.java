package dev.ftb.mods.ftblibrary.icon;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import dev.ftb.mods.ftblibrary.client.icon.ItemIconRenderer;
import dev.ftb.mods.ftblibrary.util.Lazy;
import dev.ftb.mods.ftblibrary.util.RegistryHelper;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class ItemIcon extends Icon<ItemIcon> implements IResourceIcon {
    // important to an ItemStackTemplate here; a full ItemStack will cause problems for early-loading
    // e.g. sidebar Json loading
    private final ItemStackTemplate stack;

    private ItemIcon(ItemStackTemplate is) {
        stack = is;
    }

    public static Icon<?> ofItemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return empty();
        } else if (stack.getItem() instanceof CustomIconItem c) {
            return c.getCustomIcon(stack);
        }

        return new ItemIcon(ItemStackTemplate.fromNonEmptyStack(stack));
    }

    public static Icon<?> ofItem(Item item) {
        return item == Items.AIR ? empty() : new ItemIcon(new ItemStackTemplate(item));
    }

    public static Icon<?> parse(String lazyStackString) {
        if (lazyStackString.isEmpty()) {
            return empty();
        }

        return new LazyIcon(Lazy.of(() -> {
            var fields = lazyStackString.split(" ", 4);
            var item = BuiltInRegistries.ITEM.get(Identifier.parse(fields[0])).orElse(Items.BARRIER.builtInRegistryHolder());
            int count = fields.length >= 2 && !fields[1].equals("1") ? Integer.parseInt(fields[1]) : 1;

            DataComponentPatch patch = DataComponentPatch.EMPTY;
            if (fields.length >= 4 && !fields[3].equals("null")) {
                try {
                    CompoundTag tag = TagParser.parseCompoundFully(fields[3]);
                    tag.putInt("minecraft:damage", Integer.parseInt(fields[2]));
                    patch = DataComponentPatch.CODEC.parse(NbtOps.INSTANCE, tag)
                            .resultOrPartial(err -> FTBLibrary.LOGGER.error("can't parse data component patch for {}: {}", fields[3], err))
                            .orElse(DataComponentPatch.EMPTY);
                } catch (CommandSyntaxException ex) {
                    FTBLibrary.LOGGER.error("can't parse data component tag for item icon: {} ({})", lazyStackString, ex.getMessage());
                }
            } else if (fields.length >= 3 && !fields[2].equals("0")) {
                patch = DataComponentPatch.builder().set(DataComponents.DAMAGE, Integer.parseInt(fields[2])).build();
            }

            return new ItemIcon(new ItemStackTemplate(item, count, patch));
        })) {
            @Override
            public String toString() {
                return "item:" + lazyStackString;
            }
        };
    }

    public ItemStackTemplate getStack() {
        return stack;
    }

    public String toString() {
        var stack = getStack();
        var builder = new StringBuilder("item:");
        builder.append(RegistryHelper.getIdentifier(stack.item().value(), Registries.ITEM));
        var count = stack.count();
        var nbt = DataComponentPatch.CODEC.encodeStart(NbtOps.INSTANCE, stack.components()).result()
                .orElse(null);
        var damage = nbt instanceof CompoundTag c ? c.getIntOr("minecraft:damage", 0) : 0;

        if (count > 1 || damage > 0 || nbt != null) {
            builder.append(' ').append(count);
        }
        if (damage > 0 || nbt != null) {
            builder.append(' ').append(damage);
        }
        if (nbt != null) {
            builder.append(' ').append(nbt);
        }

        return builder.toString();
    }

    public int hashCode() {
        return getStack().hashCode();
    }

    public boolean equals(Object o) {
        return o == this || o instanceof ItemIcon && getStack().equals(((ItemIcon) o).getStack());
    }

    @Override
    @Nullable
    public Object getIngredient() {
        return getStack().create();
    }

    @Override
    public IconRenderer<ItemIcon> getRenderer() {
        return ItemIconRenderer.INSTANCE;
    }

    @Override
    public Identifier getResourceId() {
        return BuiltInRegistries.ITEM.getKey(stack.item().value());
    }
}
