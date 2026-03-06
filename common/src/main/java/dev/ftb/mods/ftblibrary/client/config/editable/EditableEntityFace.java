package dev.ftb.mods.ftblibrary.client.config.editable;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.EntityFaceResource;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectEntityFaceScreen;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectableResource;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import java.util.OptionalLong;

public class EditableEntityFace extends EditableResource<EntityType<?>> {
    // any non-living entity would be ok here
    public static final EntityType<?> NONE = EntityType.AREA_EFFECT_CLOUD;

    public EditableEntityFace() {
        value = NONE;
    }

    @Override
    public boolean canHaveNBT() {
        return false;
    }

    @Override
    public OptionalLong fixedResourceSize() {
        return OptionalLong.of(1L);
    }

    @Override
    public boolean isEmpty() {
        return value == NONE;
    }

    @Override
    public SelectableResource<EntityType<?>> getResource() {
        return new EntityFaceResource(getValue());
    }

    @Override
    public boolean setResource(SelectableResource<EntityType<?>> selectable) {
        return updateValue(selectable.resource());
    }

    @Override
    public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
        new SelectEntityFaceScreen(this, callback).withGridSize(8, 12).openGui();
    }

    @Override
    public void addInfo(TooltipList list, Theme theme) {
        if (!value.equals(defaultValue)) {
            list.add(Component.translatable("config.group.value").append(": ").withStyle(ChatFormatting.AQUA)
                    .append(value.getDescription().copy().withStyle(ChatFormatting.WHITE)));
        }

        super.addInfo(list, theme);
    }

    @Override
    public Component getStringForGUI(EntityType<?> value) {
        return value == NONE ?
                Component.translatable("gui.none").withStyle(ChatFormatting.GRAY) :
                value.getDescription();
    }
}
