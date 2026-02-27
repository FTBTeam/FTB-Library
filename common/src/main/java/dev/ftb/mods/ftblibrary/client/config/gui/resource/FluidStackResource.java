package dev.ftb.mods.ftblibrary.client.config.gui.resource;

import dev.architectury.fluid.FluidStack;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public record FluidStackResource(FluidStack resource) implements SelectableResource<FluidStack> {
    @Override
    public long getCount() {
        return resource().getAmount();
    }

    @Override
    public void setCount(int count) {
        resource.setAmount(count);
    }

    @Override
    public Component getName() {
        return resource.getName();
    }

    @Override
    public Icon<?> getIcon() {
        return isEmpty() ?
                Icon.empty() :
                Icon.getIcon(ClientUtils.getStillTexture(resource)).withTint(Color4I.rgb(ClientUtils.getFluidColor(resource)));
    }

    @Override
    public SelectableResource<FluidStack> copyWithCount(long count) {
        return SelectableResource.fluid(resource.copyWithAmount(count));
    }

    @Override
    @Nullable
    public CompoundTag getComponentsTag() {
        Tag tag = DataComponentMap.CODEC.encodeStart(NbtOps.INSTANCE, resource.getComponents()).result()
                .orElse(new CompoundTag());
        return tag instanceof CompoundTag t ? t : null;
    }

    @Override
    public void applyComponentsTag(CompoundTag tag) {
        DataComponentMap.CODEC.parse(NbtOps.INSTANCE, tag).result()
                .ifPresent(resource::applyComponents);
    }
}
