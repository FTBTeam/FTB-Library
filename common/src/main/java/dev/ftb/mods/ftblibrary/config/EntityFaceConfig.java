package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.config.ui.resource.EntityFaceResource;
import dev.ftb.mods.ftblibrary.config.ui.resource.SelectEntityFaceScreen;
import dev.ftb.mods.ftblibrary.config.ui.resource.SelectableResource;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;

import java.util.OptionalLong;

public class EntityFaceConfig extends ResourceConfigValue<EntityType<?>> {
    // any non-living entity would be ok here
    public static final EntityType<?> NONE = EntityType.AREA_EFFECT_CLOUD;

    public EntityFaceConfig() {
        value = NONE;
    }

    @Override
    public OptionalLong fixedResourceSize() {
        return OptionalLong.of(1L);
    }

    @Override
    public boolean isEmpty() {
        return value == null || value == NONE;
    }

    @Override
    public SelectableResource<EntityType<?>> getResource() {
        return new EntityFaceResource(getValue());
    }

    @Override
    public boolean setResource(SelectableResource<EntityType<?>> selectable) {
        return setCurrentValue(selectable.resource());
    }

    @Override
    public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
        new SelectEntityFaceScreen(this, callback).withGridSize(8, 12).openGui();
    }

    @Override
    public void addInfo(TooltipList list) {
        if (value != null && !value.equals(defaultValue)) {
            list.add(Component.translatable("config.group.value").append(": ").withStyle(ChatFormatting.AQUA)
                    .append(getValue().getDescription().copy().withStyle(ChatFormatting.WHITE)));
        }

        super.addInfo(list);
    }

    @Override
    public Component getStringForGUI(@Nullable EntityType<?> v) {
        return v == null || v == NONE ?
                Component.translatable("gui.none").withStyle(ChatFormatting.GRAY) :
                v.getDescription();
    }
}
