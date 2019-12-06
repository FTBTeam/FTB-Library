package com.feed_the_beast.mods.ftbguilibrary.config;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public class ConfigNBT extends ConfigFromString<CompoundNBT>
{
	@Override
	public CompoundNBT copy(CompoundNBT v)
	{
		return v.copy();
	}

	@Override
	public String getStringFromValue(@Nullable CompoundNBT v)
	{
		return v == null ? "null" : v.toString();
	}

	@Override
	public String getStringForGUI(@Nullable CompoundNBT v)
	{
		return v == null ? "null" : v.isEmpty() ? "{}" : "{...}";
	}

	@Override
	public boolean parse(@Nullable Consumer<CompoundNBT> callback, String string)
	{
		if (string.equals("null"))
		{
			if (callback != null)
			{
				callback.accept(null);
			}

			return true;
		}

		try
		{
			CompoundNBT nbt = JsonToNBT.getTagFromJson(string);

			if (callback != null)
			{
				callback.accept(nbt);
			}

			return true;
		}
		catch (Exception ex)
		{
			return false;
		}
	}

	@Override
	public void addInfo(List<String> list)
	{
		list.add(TextFormatting.AQUA + "Value: " + TextFormatting.RESET + (value == null ? "null" : value.toFormattedComponent().getFormattedText()));
		list.add(TextFormatting.AQUA + "Default: " + TextFormatting.RESET + (defaultValue == null ? "null" : defaultValue.toFormattedComponent().getFormattedText()));
	}
}