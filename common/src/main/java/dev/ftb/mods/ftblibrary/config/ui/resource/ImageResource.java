package dev.ftb.mods.ftblibrary.config.ui.resource;

import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ImageResource implements SelectableResource<ResourceLocation> {
    private final ResourceLocation location;
    private final Component name;
    private final Icon icon;

    public ImageResource(ResourceLocation location) {
        this.location = location;

        name = location == null ? Component.translatable("gui.none").withStyle(ChatFormatting.GRAY) :
                Component.literal(location.getNamespace()).withStyle(ChatFormatting.GOLD).append(":")
                        .append(Component.literal(location.getPath()).withStyle(ChatFormatting.YELLOW));
        icon = Icon.getIcon(location);
    }

    @Override
    public ResourceLocation resource() {
        return location;
    }

    @Override
    public long getCount() {
        return 1;
    }

    @Override
    public void setCount(int count) {
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
}
