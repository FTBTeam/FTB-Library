package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

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
	public String getStringFromValue(@Nullable Double v)
	{
		if (v == null)
		{
			return "null";
		}
		else if (v == Double.POSITIVE_INFINITY)
		{
			return "+Inf";
		}
		else if (v == Double.NEGATIVE_INFINITY)
		{
			return "-Inf";
		}

		return super.getStringFromValue(v);
	}

	@Override
	public boolean parse(@Nullable Consumer<Double> callback, String string)
	{
		if (string.equals("+Inf"))
		{
			if (max == Double.POSITIVE_INFINITY)
			{
				if (callback != null)
				{
					callback.accept(Double.POSITIVE_INFINITY);
				}

				return true;
			}

			return false;
		}
		else if (string.equals("-Inf"))
		{
			if (min == Double.NEGATIVE_INFINITY)
			{
				if (callback != null)
				{
					callback.accept(Double.NEGATIVE_INFINITY);
				}

				return true;
			}

			return false;
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

			double v = Double.parseDouble(string.trim()) * multiplier;

			if (v >= min && v <= max)
			{
				if (callback != null)
				{
					callback.accept(v);
				}

				return true;
			}
		}
		catch (Exception ex)
		{
		}

		return false;
	}
}