package dev.ftb.mods.ftblibrary.config.ui.resource;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.util.client.ClientUtils;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

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
    public Icon getIcon() {
        return ItemIcon.getItemIcon(resource);
    }

    @Override
    public SelectableResource<ItemStack> copyWithCount(long count) {
        return SelectableResource.item(resource.copyWithCount((int) count));
    }

    @Override
    public CompoundTag getComponentsTag() {
        Tag tag = DataComponentMap.CODEC
                .encodeStart(ClientUtils.registryAccess().createSerializationContext(NbtOps.INSTANCE), resource.getComponents())
                .result()
                .orElse(new CompoundTag());
        return tag instanceof CompoundTag t ? t : null;
    }

    @Override
    public void applyComponentsTag(CompoundTag tag) {
        DataComponentMap.CODEC
                .parse(ClientUtils.registryAccess().createSerializationContext(NbtOps.INSTANCE), tag)
                .result()
                .ifPresent(resource::applyComponents);
    }
}
