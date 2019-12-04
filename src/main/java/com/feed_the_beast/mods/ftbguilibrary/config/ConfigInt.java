package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.Optional;

/**
 * @author LatvianModder
 */
public class ConfigInt extends ConfigNumber<Integer>
{
	public ConfigInt(int mn, int mx)
	{
		super(mn, mx);
	}

	@Override
	public boolean isValid(Integer value)
	{
		return value >= min && value <= max;
	}

	@Override
	public void addInfo(List<String> list)
	{
		super.addInfo(list);

		if (min != Integer.MIN_VALUE)
		{
			list.add(TextFormatting.AQUA + "Min: " + TextFormatting.RESET + StringUtils.formatDouble(min));
		}

		if (max != Integer.MAX_VALUE)
		{
			list.add(TextFormatting.AQUA + "Max: " + TextFormatting.RESET + StringUtils.formatDouble(max));
		}
	}

	@Override
	public Optional<Integer> getValueFromString(String string)
	{
		if (string.isEmpty())
		{
			return Optional.empty();
		}

		try
		{
			return Optional.of(Long.decode(string).intValue());
		}
		catch (Exception ex)
		{
			return Optional.empty();
		}
	}
}