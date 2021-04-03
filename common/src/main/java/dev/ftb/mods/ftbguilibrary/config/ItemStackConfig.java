package dev.ftb.mods.ftbguilibrary.config;

import dev.ftb.mods.ftbguilibrary.config.gui.SelectItemStackScreen;
import dev.ftb.mods.ftbguilibrary.utils.MouseButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * @author LatvianModder
 */
public class ItemStackConfig extends ConfigValue<ItemStack> {
	public final boolean singleItemOnly;
	public final boolean allowEmpty;

	public ItemStackConfig(boolean single, boolean empty) {
		singleItemOnly = single;
		allowEmpty = empty;
		defaultValue = ItemStack.EMPTY;
		value = ItemStack.EMPTY;
	}

	@Override
	public ItemStack copy(ItemStack value) {
		return value.isEmpty() ? ItemStack.EMPTY : value.copy();
	}

	@Override
	public Component getStringForGUI(@Nullable ItemStack v) {
		if (v == null || v.isEmpty()) {
			return ItemStack.EMPTY.getHoverName();
		} else if (v.getCount() <= 1) {
			return v.getHoverName();
		}

		return new TextComponent(v.getCount() + "x ").append(v.getHoverName());
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback) {
		if (getCanEdit()) {
			new SelectItemStackScreen(this, callback).openGui();
		}
	}
}