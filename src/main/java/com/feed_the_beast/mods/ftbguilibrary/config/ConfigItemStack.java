package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiSelectItemStack;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class ConfigItemStack extends ConfigValue<ItemStack>
{
	public final boolean singleItemOnly;
	public final boolean allowEmpty;

	public ConfigItemStack(boolean single, boolean empty)
	{
		singleItemOnly = single;
		allowEmpty = empty;
		defaultValue = ItemStack.EMPTY;
		value = ItemStack.EMPTY;
	}

	@Override
	public ItemStack copy(ItemStack value)
	{
		return value.isEmpty() ? ItemStack.EMPTY : value.copy();
	}

	@Override
	public String getStringForGUI(@Nullable ItemStack v)
	{
		if (v == null || v.isEmpty())
		{
			return ItemStack.EMPTY.getDisplayName().getFormattedText();
		}
		else if (v.getCount() <= 1)
		{
			return v.getDisplayName().getFormattedText();
		}

		return new StringTextComponent(v.getCount() + "x ").appendSibling(v.getDisplayName()).getFormattedText();
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback)
	{
		if (getCanEdit())
		{
			new GuiSelectItemStack(this, callback).openGui();
		}
	}
}