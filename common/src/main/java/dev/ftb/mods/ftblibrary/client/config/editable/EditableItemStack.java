package dev.ftb.mods.ftblibrary.client.config.editable;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectableResource;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.Validate;
import org.jspecify.annotations.Nullable;

import java.util.OptionalLong;

public class EditableItemStack extends EditableResource<ItemStack> {
    private final boolean allowEmpty;
    private final boolean isFixedSize;
    private final long fixedSize;

    public EditableItemStack(boolean single, boolean empty) {
        isFixedSize = single && !empty;
        fixedSize = 0L;
        allowEmpty = empty;
        defaultValue = ItemStack.EMPTY;
        value = ItemStack.EMPTY;
    }

    public EditableItemStack(long fixedSize) {
        Validate.isTrue(fixedSize >= 1);
        this.isFixedSize = true;
        this.fixedSize = fixedSize;
        allowEmpty = false;
        defaultValue = ItemStack.EMPTY;
        value = ItemStack.EMPTY;
    }

    @Override
    public ItemStack copy(ItemStack value) {
        return value.isEmpty() ? ItemStack.EMPTY : value.copy();
    }

    @Override
    public Component getStringForGUI(@Nullable ItemStack value) {
        if (value == null || value.isEmpty()) {
            return Component.translatable("gui.none");
        } else if (value.getCount() <= 1) {
            return value.getHoverName();
        }

        return Component.literal(value.getCount() + "x ").append(value.getHoverName());
    }

    @Override
    public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
        if (getCanEdit()) {
            new SelectItemStackScreen(this, callback).openGui();
        }
    }

    @Override
    public ItemStack getValue() {
        ItemStack val = super.getValue();
        return val.isEmpty() ? ItemStack.EMPTY : val;
    }

    @Override
    public boolean allowEmptyResource() {
        return allowEmpty;
    }

    @Override
    public OptionalLong fixedResourceSize() {
        return isFixedSize ? OptionalLong.of(fixedSize) : OptionalLong.empty();
    }

    @Override
    public boolean isEmpty() {
        return getValue().isEmpty();
    }

    @Override
    public SelectableResource<ItemStack> getResource() {
        return SelectableResource.item(getValue());
    }

    @Override
    public boolean setResource(SelectableResource<ItemStack> selectable) {
        return updateValue(selectable.resource());
    }
}
