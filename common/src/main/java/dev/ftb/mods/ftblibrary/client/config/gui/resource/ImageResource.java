package dev.ftb.mods.ftblibrary.client.config.gui.resource;

import dev.ftb.mods.ftblibrary.client.config.editable.EditableImageResource;
import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ImageResource implements SelectableResource<Identifier> {
    private final Identifier location;
    private final Component name;
    private final Icon<?> icon;

    public ImageResource(Identifier location) {
        this.location = location;

        name = location.equals(EditableImageResource.NONE) ? Component.translatable("gui.none").withStyle(ChatFormatting.GRAY) :
                Component.literal(location.getNamespace()).withStyle(ChatFormatting.GOLD).append(":")
                        .append(Component.literal(location.getPath()).withStyle(ChatFormatting.YELLOW));
        icon = Icon.getIcon(location);
    }

    @Override
    public Identifier resource() {
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
    public Icon<?> getIcon() {
        return icon;
    }

    @Override
    public SelectableResource<Identifier> copyWithCount(long count) {
        return this;
    }
}
