package com.feed_the_beast.mods.ftbguilibrary.widget;


import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;

import java.util.List;

/**
 * @author LatvianModder
 */
public abstract class SimpleTextButton extends Button
{
	public SimpleTextButton(Panel panel, String txt, Icon icon)
	{
		super(panel, txt, icon);
		setWidth(panel.getGui().getTheme().getStringWidth(txt) + (hasIcon() ? 28 : 8));
		setHeight(20);
	}

	@Override
	public SimpleTextButton setTitle(String txt)
	{
		super.setTitle(txt);
		setWidth(getGui().getTheme().getStringWidth(getTitle()) + (hasIcon() ? 28 : 8));
		return this;
	}

	public boolean renderTitleInCenter()
	{
		return false;
	}

	@Override
	public Object getIngredientUnderMouse()
	{
		return icon.getIngredient();
	}

	public boolean hasIcon()
	{
		return !icon.isEmpty();
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		if (getGui().getTheme().getStringWidth(getTitle()) + (hasIcon() ? 28 : 8) > width)
		{
			list.add(getTitle());
		}
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		drawBackground(theme, x, y, w, h);
		int s = h >= 16 ? 16 : 8;
		int off = (h - s) / 2;
		String title = getTitle();
		int textX = x;
		int textY = y + (h - theme.getFontHeight() + 1) / 2;

		int sw = theme.getStringWidth(title);
		int mw = w - (hasIcon() ? off + s : 0) - 6;

		if (sw > mw)
		{
			sw = mw;
			title = theme.trimStringToWidth(title, mw);
		}

		if (renderTitleInCenter())
		{
			textX += (mw - sw + 6) / 2;
		}
		else
		{
			textX += 4;
		}

		if (hasIcon())
		{
			drawIcon(theme, x + off, y + off, s, s);
			textX += off + s;
		}

		theme.drawString(title, textX, textY, theme.getContentColor(getWidgetType()), Theme.SHADOW);
	}
}