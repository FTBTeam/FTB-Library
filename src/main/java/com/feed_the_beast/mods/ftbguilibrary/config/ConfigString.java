package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class ConfigString extends ConfigFromString<String>
{
	public static final Color4I COLOR = Color4I.rgb(0xFFAA49);

	public final Pattern pattern;

	public ConfigString(@Nullable Pattern p)
	{
		pattern = p;
	}

	@Override
	public boolean isValid(String value)
	{
		return pattern == null || pattern.matcher(value).matches();
	}

	@Override
	public Color4I getColor(String value)
	{
		return COLOR;
	}

	@Override
	public String getStringForGUI(String value)
	{
		return '"' + value + '"';
	}

	@Override
	public Optional<String> getValueFromString(String string)
	{
		return Optional.of(string);
	}

	@Override
	public void addInfo(List<String> list)
	{
		super.addInfo(list);

		if (pattern != null)
		{
			list.add(TextFormatting.AQUA + "Regex: " + TextFormatting.RESET + pattern.pattern());
		}
	}

	@Override
	public boolean isEmpty(String value)
	{
		return value.isEmpty();
	}
}