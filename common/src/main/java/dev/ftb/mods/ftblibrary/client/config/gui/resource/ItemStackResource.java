package dev.ftb.mods.ftblibrary.client.config.gui.resource;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record ItemStackResource(ItemStack resource) implements SelectableResource<ItemStack> {
    @Override
    public long getCount() {
        return resource.getCount();
    }

    @Override
    public void setCount(int count) {
        resource.setCount(count);
    }

    @Override
    public Component getName() {
        return resource.getHoverName();
    }

    @Override
    public Icon<?> getIcon() {
        return ItemIcon.ofItemStack(resource);
    }

    @Override
    public SelectableResource<ItemStack> copyWithCount(long count) {
        return SelectableResource.item(resource.copyWithCount((int) count));
    }

    @Override
    @Nullable
    public CompoundTag getComponentsTag() {
        Tag tag = DataComponentPatch.CODEC.encodeStart(NbtOps.INSTANCE, resource.getComponentsPatch()).result()
                .orElse(new CompoundTag());
        return tag instanceof CompoundTag t ? t : null;
    }

    @Override
    public void applyComponentsTag(CompoundTag tag) {
        DataComponentMap.CODEC.parse(NbtOps.INSTANCE, tag).result()
                .ifPresent(resource::applyComponents);
    }
}
