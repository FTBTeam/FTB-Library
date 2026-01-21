package dev.ftb.mods.ftblibrary.client.config.editable;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.ImageResource;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectImageResourceScreen;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectableResource;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.icon.IResourceIcon;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.OptionalLong;

public class EditableImageResource extends EditableResource<Identifier> {
    public static final Identifier NONE = FTBLibrary.rl("none");

    public EditableImageResource() {
        value = NONE;
    }

    public static Identifier getIdentifier(Icon icon) {
        return icon instanceof IResourceIcon i ? i.getResourceId() : NONE;
    }

    @Override
    public void onClicked(Widget clicked, MouseButton button, ConfigCallback callback) {
        new SelectImageResourceScreen(this, callback).withGridSize(8, 12).openGui();
    }

    @Override
    public OptionalLong fixedResourceSize() {
        return OptionalLong.of(1L);
    }

    @Override
    public boolean isEmpty() {
        return value == null || value.equals(NONE);
    }

    @Override
    public SelectableResource<Identifier> getResource() {
        return new ImageResource(getValue());
    }

    @Override
    public boolean setResource(SelectableResource<Identifier> selectable) {
        return updateValue(selectable.resource());
    }

    @Override
    public void addInfo(TooltipList list) {
        if (value != null && !value.equals(defaultValue)) {
            list.add(Component.translatable("config.group.value").append(": ").withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(value.toString()).withStyle(ChatFormatting.WHITE)));
        }

        super.addInfo(list);
    }
}
