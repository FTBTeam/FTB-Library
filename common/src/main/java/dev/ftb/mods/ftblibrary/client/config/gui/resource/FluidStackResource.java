package dev.ftb.mods.ftblibrary.client.config.gui.resource;

import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.client.util.TextureAtlasSpriteRef;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.TextureAtlasSpriteIcon;
import dev.ftb.mods.ftblibrary.platform.fluid.FluidStack;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public record FluidStackResource(FluidStack resource) implements SelectableResource<FluidStack> {
    @Override
    public long getCount() {
        return resource().amount();
    }

    @Override
    public void setCount(int count) {
        resource.setAmount(count);
    }

    @Override
    public Component getName() {
        return resource.name();
    }

    @Override
    public Icon<?> getIcon() {
        return isEmpty() ?
                Icon.empty() :
                new TextureAtlasSpriteRef(ClientUtils.getStillTexture(resource)).createIcon(Color4I.rgb(ClientUtils.getFluidColor(resource)));
    }

    @Override
    public SelectableResource<FluidStack> copyWithCount(long count) {
        return SelectableResource.fluid(resource.copyWithAmount(count));
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
