package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfigList;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ConfigList<E, CV extends ConfigValue<E>> extends ConfigValue<List<E>>
{
	public static final Color4I COLOR = Color4I.rgb(0xFFAA49);
	public final CV type;

	public ConfigList(CV t)
	{
		type = t;
	}

	@Override
	public List<E> copy(List<E> v)
	{
		List<E> list = new ArrayList<>(v.size());

		for (E value : v)
		{
			list.add(type.copy(value));
		}

		return list;
	}

	@Override
	public Color4I getColor(List<E> v)
	{
		return COLOR;
	}

	@Override
	public void addInfo(List<String> l)
	{
		if (current.isEmpty())
		{
			l.add(TextFormatting.AQUA + "Value: []");
		}
		else
		{
			l.add(TextFormatting.AQUA + "Value: [");

			for (E value : current)
			{
				l.add("  " + type.getStringForGUI(value));
			}

			l.add(TextFormatting.AQUA + "]");
		}

		if (defaultValue.isEmpty())
		{
			l.add(TextFormatting.AQUA + "Default: []");
		}
		else
		{
			l.add(TextFormatting.AQUA + "Default: [");

			for (E value : defaultValue)
			{
				l.add("  " + type.getStringForGUI(value));
			}

			l.add(TextFormatting.AQUA + "]");
		}
	}

	@Override
	public void onClicked(MouseButton button, Runnable callback)
	{
		new GuiEditConfigList<>(this, callback).openGui();
	}

	@Override
	public String getStringForGUI(List<E> value)
	{
		return value.isEmpty() ? "[]" : "[...]";
	}

	@Override
	public boolean isEmpty(List<E> value)
	{
		return value.isEmpty();
	}
}