package dev.ftb.mods.ftblibrary.client.config.editable;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectFluidScreen;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectableResource;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.widget.Widget;
import dev.ftb.mods.ftblibrary.platform.Platform;
import dev.ftb.mods.ftblibrary.platform.fluid.FluidStack;
import net.minecraft.network.chat.Component;

import java.util.OptionalLong;

public class EditableFluid extends EditableResource<FluidStack> {
    private final boolean allowEmpty;
    private final boolean isFixedSize;
    private final long fixedSize;
    private boolean showAmount = true;

    public EditableFluid(boolean allowEmpty) {
        this.isFixedSize = false;
        this.fixedSize = 0;
        this.allowEmpty = allowEmpty;
        defaultValue = FluidStack.empty();
        value = FluidStack.empty();
    }

    public EditableFluid(long fixedSize) {
        this.isFixedSize = true;
        this.fixedSize = fixedSize;
        this.allowEmpty = false;
        defaultValue = FluidStack.empty();
        value = FluidStack.empty();
    }

    public EditableFluid showAmount(boolean show) {
        showAmount = show;
        return this;
    }

    @Override
    public Component getStringForGUI(FluidStack value) {
        if (value.isEmpty()) {
            return Component.translatable("gui.none");
        }

        // TODO: Figure out if "droplets" is the preferred term for fabric.
        return showAmount ? Component.literal(value.amount() + (Platform.get().isFabric() ? " droplets " :"mB ")).append(value.name()) : value.name();
    }

    @Override
    public void onClicked(Widget clickedWidget, MouseButton button, ConfigCallback callback) {
        if (getCanEdit()) {
            new SelectFluidScreen(this, callback).openGui();
        }
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
        return value == null || value.isEmpty();
    }

    @Override
    public SelectableResource<FluidStack> getResource() {
        return SelectableResource.fluid(getValue());
    }

    @Override
    public boolean setResource(SelectableResource<FluidStack> selectable) {
        return updateValue(selectable.resource());
    }
}
