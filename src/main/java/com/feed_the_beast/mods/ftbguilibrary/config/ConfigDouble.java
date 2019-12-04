package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.Optional;

/**
 * @author LatvianModder
 */
public class ConfigDouble extends ConfigNumber<Double>
{
	public ConfigDouble(double mn, double mx)
	{
		super(mn, mx);
	}

	@Override
	public boolean isValid(Double value)
	{
		return value >= min && value <= max;
	}

	@Override
	public void addInfo(List<String> list)
	{
		super.addInfo(list);

		if (min != Double.NEGATIVE_INFINITY)
		{
			list.add(TextFormatting.AQUA + "Min: " + TextFormatting.RESET + StringUtils.formatDouble(min));
		}

		if (max != Double.POSITIVE_INFINITY)
		{
			list.add(TextFormatting.AQUA + "Max: " + TextFormatting.RESET + StringUtils.formatDouble(max));
		}
	}

	@Override
	public Optional<Double> getValueFromString(String string)
	{
		if (string.isEmpty())
		{
			return Optional.empty();
		}
		else if (string.equals("+Inf"))
		{
			return Optional.of(Double.POSITIVE_INFINITY);
		}
		else if (string.equals("-Inf"))
		{
			return Optional.of(Double.NEGATIVE_INFINITY);
		}

		try
		{
			double multiplier = 1D;

			if (string.endsWith("K"))
			{
				multiplier = 1000D;
				string = string.substring(0, string.length() - 1);
			}
			else if (string.endsWith("M"))
			{
				multiplier = 1000000D;
				string = string.substring(0, string.length() - 1);
			}
			else if (string.endsWith("B"))
			{
				multiplier = 1000000000D;
				string = string.substring(0, string.length() - 1);
			}

			return Optional.of(Double.parseDouble(string.trim()) * multiplier);
		}
		catch (Exception ex)
		{
			return Optional.empty();
		}
	}
}