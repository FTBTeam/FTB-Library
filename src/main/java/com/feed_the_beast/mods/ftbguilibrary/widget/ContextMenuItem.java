package com.feed_the_beast.mods.ftbguilibrary.widget;

import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * @author LatvianModder
 */
public class ContextMenuItem implements Comparable<ContextMenuItem>
{
	public static final ContextMenuItem SEPARATOR = new ContextMenuItem(StringTextComponent.EMPTY, Icon.EMPTY, () -> {})
	{
		@Override
		public Widget createWidget(ContextMenu panel)
		{
			return new ContextMenu.CSeperator(panel);
		}
	};

	public static final BooleanSupplier TRUE = () -> true;
	public static final BooleanSupplier FALSE = () -> false;

	public ITextComponent title;
	public Icon icon;
	public Runnable callback;
	public BooleanSupplier enabled = TRUE;
	public ITextComponent yesNoText = new StringTextComponent("");
	public boolean closeMenu = true;

	public ContextMenuItem(ITextComponent t, Icon i, @Nullable Runnable c)
	{
		title = t;
		icon = i;
		callback = c;
	}

	public void addMouseOverText(List<ITextProperties> list)
	{
	}

	public void drawIcon(Theme theme, int x, int y, int w, int h)
	{
		icon.draw(x, y, w, h);
	}

	public ContextMenuItem setEnabled(boolean v)
	{
		return setEnabled(v ? TRUE : FALSE);
	}

	public ContextMenuItem setEnabled(BooleanSupplier v)
	{
		enabled = v;
		return this;
	}

	public ContextMenuItem setYesNo(ITextComponent s)
	{
		yesNoText = s;
		return this;
	}

	public ContextMenuItem setCloseMenu(boolean v)
	{
		closeMenu = v;
		return this;
	}

	public Widget createWidget(ContextMenu panel)
	{
		return new ContextMenu.CButton(panel, this);
	}

	@Override
	public int compareTo(ContextMenuItem o)
	{
		return title.getString().compareToIgnoreCase(o.title.getString());
	}

	public void onClicked(Panel panel, MouseButton button)
	{
		if (closeMenu)
		{
			panel.getGui().closeContextMenu();
		}

		callback.run();
	}
}