package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.misc.GuiButtonListBase;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * @author LatvianModder
 */
public class ConfigEnum<E> extends ConfigWithVariants<E>
{
	public static final Color4I COLOR = Color4I.rgb(0x0094FF);

	public final NameMap<E> nameMap;

	public ConfigEnum(NameMap<E> nm)
	{
		nameMap = nm;
	}

	@Override
	public String getStringForGUI(E value)
	{
		return nameMap.getDisplayName(value).getFormattedText();
	}

	@Override
	public Color4I getColor(E value)
	{
		Color4I col = nameMap.getColor(value);
		return col.isEmpty() ? COLOR : col;
	}

	@Override
	public void addInfo(List<String> list)
	{
		super.addInfo(list);

		if (nameMap.size() > 0)
		{
			list.add("");

			for (E v : nameMap)
			{
				list.add((isEqual(v, current) ? (TextFormatting.AQUA + "+ ") : (TextFormatting.DARK_GRAY + "- ")) + nameMap.getDisplayName(v).getString());
			}
		}
	}

	@Override
	public void onClicked(MouseButton button, Runnable callback)
	{
		if (nameMap.values.size() > 16 || GuiBase.isCtrlKeyDown())
		{
			GuiButtonListBase g = new GuiButtonListBase()
			{
				@Override
				public void addButtons(Panel panel)
				{
					for (E v : nameMap)
					{
						panel.add(new SimpleTextButton(panel, nameMap.getDisplayName(v).getString(), Icon.EMPTY)
						{
							@Override
							public void onClicked(MouseButton button)
							{
								playClickSound();
								setCurrentValue(v);
								callback.run();
							}
						});
					}
				}
			};

			g.setHasSearchBox(true);
			g.openGui();
			return;
		}

		super.onClicked(button, callback);
	}

	@Override
	public List<E> getVariants()
	{
		return nameMap.values;
	}

	@Override
	public E getIteration(E value, boolean next)
	{
		return next ? nameMap.getNext(value) : nameMap.getPrevious(value);
	}
}