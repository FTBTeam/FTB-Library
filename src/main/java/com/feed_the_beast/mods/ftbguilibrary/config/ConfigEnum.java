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
	public String getStringForGUI(E v)
	{
		return nameMap.getDisplayName(v).getFormattedText();
	}

	@Override
	public Color4I getColor(E v)
	{
		Color4I col = nameMap.getColor(v);
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
				list.add((isEqual(v, value) ? (TextFormatting.AQUA + "+ ") : (TextFormatting.DARK_GRAY + "- ")) + nameMap.getDisplayName(v).getString());
			}
		}
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback)
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
								callback.save(true);
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
	public E getIteration(E v, boolean next)
	{
		return next ? nameMap.getNext(v) : nameMap.getPrevious(v);
	}
}