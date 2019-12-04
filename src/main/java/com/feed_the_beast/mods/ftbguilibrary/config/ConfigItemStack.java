package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiSelectItemStack;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

/**
 * @author LatvianModder
 */
public class ConfigItemStack extends ConfigValue<ItemStack>
{
	public final boolean singleItemOnly;
	public final boolean allowEmpty;

	public ConfigItemStack(boolean b, boolean e)
	{
		singleItemOnly = b;
		allowEmpty = e;
	}

	@Override
	public ItemStack copy(ItemStack value)
	{
		return value.copy();
	}

	@Override
	public boolean isEmpty(ItemStack value)
	{
		return value.isEmpty();
	}

	@Override
	public boolean isValid(ItemStack value)
	{
		if (!allowEmpty && value.isEmpty())
		{
			return false;
		}

		return !singleItemOnly || value.getCount() <= 1;
	}

	@Override
	public String getStringForGUI(ItemStack value)
	{
		if (value.getCount() <= 1)
		{
			return value.getDisplayName().getFormattedText();
		}

		return new StringTextComponent(value.getCount() + "x ").appendSibling(value.getDisplayName()).getFormattedText();
	}

	@Override
	public void onClicked(MouseButton button, Runnable callback)
	{
		if (canEdit)
		{
			new GuiSelectItemStack(this, callback).openGui();
		}
	}
}