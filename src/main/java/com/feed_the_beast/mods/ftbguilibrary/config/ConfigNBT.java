package com.feed_the_beast.mods.ftbguilibrary.config;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.Optional;

/**
 * @author LatvianModder
 */
public class ConfigNBT extends ConfigFromString<CompoundNBT>
{
	@Override
	public CompoundNBT copy(CompoundNBT value)
	{
		return value.copy();
	}

	@Override
	public String getStringFromValue(CompoundNBT value)
	{
		return value.toString();
	}

	@Override
	public String getStringForGUI(CompoundNBT value)
	{
		return value.isEmpty() ? "{}" : "{...}";
	}

	@Override
	public Optional<CompoundNBT> getValueFromString(String string)
	{
		if (string.equals("null") || string.equals("{}"))
		{
			return Optional.of(new CompoundNBT());
		}

		try
		{
			return Optional.of(JsonToNBT.getTagFromJson(string));
		}
		catch (Exception ex)
		{
			return Optional.empty();
		}
	}

	@Override
	public void addInfo(List<String> list)
	{
		list.add(TextFormatting.AQUA + "Value: " + TextFormatting.RESET + current.toFormattedComponent().getFormattedText());
		list.add(TextFormatting.AQUA + "Default: " + TextFormatting.RESET + defaultValue.toFormattedComponent().getFormattedText());
	}

	@Override
	public boolean isEmpty(CompoundNBT value)
	{
		return value.isEmpty();
	}
}