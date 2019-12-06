package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author LatvianModder
 */
public class ConfigLong extends ConfigNumber<Long>
{
	public ConfigLong(long mn, long mx)
	{
		super(mn, mx);
	}

	@Override
	public void addInfo(List<String> list)
	{
		super.addInfo(list);

		if (min != Long.MIN_VALUE)
		{
			list.add(TextFormatting.AQUA + "Min: " + TextFormatting.RESET + StringUtils.formatDouble(min));
		}

		if (max != Long.MAX_VALUE)
		{
			list.add(TextFormatting.AQUA + "Max: " + TextFormatting.RESET + StringUtils.formatDouble(max));
		}
	}

	@Override
	public boolean parse(@Nullable Consumer<Long> callback, String string)
	{
		try
		{
			long v = Long.decode(string);

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