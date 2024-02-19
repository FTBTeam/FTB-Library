package dev.ftb.mods.ftblibrary.config.ui;

import dev.architectury.fluid.FluidStack;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Wraps a resource which can be searched for in a search GUI (implementations exist for items and fluids)
 * @param <T> the resource type
 */
public interface SelectableResource<T> {
    T stack();

    long getCount();

    default boolean isEmpty() {
        return getCount() == 0;
    }

    Component getName();

    Icon getIcon();

    SelectableResource<T> copyWithCount(long count);

    static SelectableResource<ItemStack> item(ItemStack stack) {
        return new ItemStackResource(stack);
    }

    static SelectableResource<FluidStack> fluid(FluidStack stack) {
        return new FluidStackResource(stack);
    }

    CompoundTag getTag();

    void setTag(CompoundTag tag);

    void setCount(int count);

    record ItemStackResource(ItemStack stack) implements SelectableResource<ItemStack> {
        @Override
        public long getCount() {
            return stack.getCount();
        }

        @Override
        public Component getName() {
            return stack.getHoverName();
        }

        @Override
        public Icon getIcon() {
            return ItemIcon.getItemIcon(stack);
        }

        @Override
        public SelectableResource<ItemStack> copyWithCount(long count) {
            return item(stack.copyWithCount((int) count));
        }

        @Override
        public CompoundTag getTag() {
            return stack.getTag();
        }

        @Override
        public void setTag(CompoundTag tag) {
            stack.setTag(tag);
        }

        @Override
        public void setCount(int count) {
            stack.setCount(count);
        }
    }

    record FluidStackResource(FluidStack stack) implements SelectableResource<FluidStack> {
        @Override
        public long getCount() {
            return stack().getAmount();
        }

        @Override
        public Component getName() {
            return stack.getName();
        }

        @Override
        public Icon getIcon() {
            return Icon.getIcon(ClientUtils.getStillTexture(stack)).withTint(Color4I.rgb(ClientUtils.getFluidColor(stack)));
        }

        @Override
        public SelectableResource<FluidStack> copyWithCount(long count) {
            return fluid(stack.copyWithAmount(count));
        }

        @Override
        public CompoundTag getTag() {
            return stack.getTag();
        }

        @Override
        public void setTag(CompoundTag tag) {
            stack.setTag(tag);
        }

        @Override
        public void setCount(int count) {
            stack.setAmount(count);
        }
    }

    class ImageResource implements SelectableResource<ResourceLocation> {
        private final ResourceLocation location;
        private final Component name;
        private final Icon icon;

        public ImageResource(ResourceLocation location) {
            this.location = location;

            name = Component.literal(location.getNamespace()).withStyle(ChatFormatting.GOLD).append(":")
                    .append(Component.literal(location.getPath()).withStyle(ChatFormatting.YELLOW));
            icon = Icon.getIcon(location);
        }

        @Override
        public ResourceLocation stack() {
            return location;
        }

        @Override
        public long getCount() {
            return 1;
        }

        @Override
        public Component getName() {
            return name;
        }

        @Override
        public Icon getIcon() {
            return icon;
        }

        @Override
        public SelectableResource<ResourceLocation> copyWithCount(long count) {
            return this;
        }

        @Override
        public CompoundTag getTag() {
            return null;
        }

        @Override
        public void setTag(CompoundTag tag) {
        }

        @Override
        public void setCount(int count) {
        }
    }
}
