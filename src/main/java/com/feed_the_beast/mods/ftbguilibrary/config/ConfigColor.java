package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;

import java.util.Optional;

/**
 * @author LatvianModder
 */
public class ConfigColor extends ConfigFromString<Color4I>
{
	@Override
	public Color4I getColor(Color4I value)
	{
		return value;
	}

	@Override
	public Optional<Color4I> getValueFromString(String string)
	{
		try
		{
			if (string.indexOf(',') != -1)
			{
				if (string.length() < 5)
				{
					return Optional.empty();
				}

				String[] s = string.split(",");

				if (s.length == 3 || s.length == 4)
				{
					int[] c = new int[4];
					c[3] = 255;

					for (int i = 0; i < s.length; i++)
					{
						c[i] = Integer.parseInt(s[i]);
					}

					return Optional.of(Color4I.rgba(c[0], c[1], c[2], c[3]));
				}
			}
			else
			{
				if (string.length() < 6)
				{
					return Optional.empty();
				}
				else if (string.startsWith("#"))
				{
					string = string.substring(1);
				}

				int hex = Integer.parseInt(string, 16);
				return Optional.of(Color4I.rgba(0xFF000000 | hex));
			}
		}
		catch (Exception ex)
		{
		}

		return Optional.empty();
	}
}