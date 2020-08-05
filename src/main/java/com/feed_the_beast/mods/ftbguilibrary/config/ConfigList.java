package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.config.gui.GuiEditConfigList;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ConfigList<E, CV extends ConfigValue<E>> extends ConfigValue<List<E>>
{
	public static final StringTextComponent EMPTY_LIST = new StringTextComponent("[]");
	public static final StringTextComponent NON_EMPTY_LIST = new StringTextComponent("[...]");

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
	public void addInfo(List<ITextProperties> l)
	{
		if (!value.isEmpty())
		{
			l.add(info("List"));

			for (E value : value)
			{
				l.add(type.getStringForGUI(value));
			}

			if (!defaultValue.isEmpty())
			{
				l.add(StringTextComponent.EMPTY);
			}
		}

		if (!defaultValue.isEmpty())
		{
			l.add(info("Default"));

			for (E value : defaultValue)
			{
				l.add(type.getStringForGUI(value));
			}
		}
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback)
	{
		new GuiEditConfigList<>(this, callback).openGui();
	}

	@Override
	public ITextComponent getStringForGUI(List<E> v)
	{
		return v == null ? NULL_TEXT : v.isEmpty() ? EMPTY_LIST : NON_EMPTY_LIST;
	}
}