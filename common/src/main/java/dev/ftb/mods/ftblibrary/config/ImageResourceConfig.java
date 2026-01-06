package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.ui.resource.ImageResource;
import dev.ftb.mods.ftblibrary.config.ui.resource.SelectImageResourceScreen;
import dev.ftb.mods.ftblibrary.config.ui.resource.SelectableResource;
import dev.ftb.mods.ftblibrary.icon.IResourceIcon;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.OptionalLong;

public class ImageResourceConfig extends ResourceConfigValue<Identifier> {
    public static final Identifier NONE = FTBLibrary.rl("none");

    public ImageResourceConfig() {
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
        return setCurrentValue(selectable.resource());
    }

    @Override
    public void addInfo(TooltipList list) {
        if (value != null && !value.equals(defaultValue)) {
            list.add(Component.translatable("config.group.value").append(": ").withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(getValue().toString()).withStyle(ChatFormatting.WHITE)));
        }

        super.addInfo(list);
    }
}
