package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;

import javax.annotation.Nullable;
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
	public void addInfo(TooltipList list)
	{
		super.addInfo(list);

		if (min != Long.MIN_VALUE)
		{
			list.add(info("Min", StringUtils.formatDouble(min)));
		}

		if (max != Long.MAX_VALUE)
		{
			list.add(info("Max", StringUtils.formatDouble(max)));
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