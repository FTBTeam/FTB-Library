package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.config.ui.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.config.ui.SelectableResource;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalLong;

public class ItemStackConfig extends ResourceConfigValue<ItemStack> {
	private final boolean allowEmpty;
	private final boolean isFixedSize;
	private final long fixedSize;

	public ItemStackConfig(boolean single, boolean empty) {
		isFixedSize = !single && !empty;
		fixedSize = 0L;
		allowEmpty = empty;
		defaultValue = ItemStack.EMPTY;
		value = ItemStack.EMPTY;
	}

	public ItemStackConfig(long fixedSize) {
		Validate.isTrue(fixedSize >= 1);
		this.isFixedSize = true;
		this.fixedSize = fixedSize;
		allowEmpty = false;
		defaultValue = ItemStack.EMPTY;
		value = ItemStack.EMPTY;
	}

	@Deprecated(forRemoval = true)
	public boolean isSingleItemOnly() {
		return isFixedSize && fixedSize == 1;
	}

	@Deprecated(forRemoval = true)
	public boolean allowEmptyItem() {
		return allowEmptyResource();
	}

	@Override
	public ItemStack copy(ItemStack value) {
		return value.isEmpty() ? ItemStack.EMPTY : value.copy();
	}

	@Override
	public Component getStringForGUI(@Nullable ItemStack v) {
		if (v == null || v.isEmpty()) {
			return Component.translatable("gui.none");
		} else if (v.getCount() <= 1) {
			return v.getHoverName();
		}

		return Component.literal(v.getCount() + "x ").append(v.getHoverName());
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
	public boolean setResource(SelectableResource<ItemStack> selectedStack) {
		return setCurrentValue(selectedStack.stack());
	}
}
