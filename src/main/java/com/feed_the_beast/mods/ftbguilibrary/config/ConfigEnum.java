package com.feed_the_beast.mods.ftbguilibrary.config;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.misc.GuiButtonListBase;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class ConfigEnum<E> extends ConfigWithVariants<E>
{
	public final NameMap<E> nameMap;

	public ConfigEnum(NameMap<E> nm)
	{
		nameMap = nm;
		defaultValue = nameMap.defaultValue;
		value = nameMap.defaultValue;
	}

	@Override
	public ITextComponent getStringForGUI(E v)
	{
		return nameMap.getDisplayName(v);
	}

	@Override
	public Color4I getColor(E v)
	{
		Color4I col = nameMap.getColor(v);
		return col.isEmpty() ? Tristate.DEFAULT.color : col;
	}

	@Override
	public void addInfo(TooltipList list)
	{
		super.addInfo(list);

		if (nameMap.size() > 0)
		{
			list.blankLine();

			for (E v : nameMap)
			{
				boolean e = isEqual(v, value);
				StringTextComponent c = new StringTextComponent(e ? "+ " : "- ");
				c.mergeStyle(e ? TextFormatting.AQUA : TextFormatting.DARK_GRAY);
				c.append(nameMap.getDisplayName(v));
				list.add(c);
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
						panel.add(new SimpleTextButton(panel, nameMap.getDisplayName(v), Icon.EMPTY)
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

	@Override
	public Icon getIcon(@Nullable E v)
	{
		if (v != null)
		{
			Icon icon = nameMap.getIcon(v);

			if (!icon.isEmpty())
			{
				return icon;
			}
		}

		return super.getIcon(v);
	}
}