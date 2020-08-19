package com.feed_the_beast.mods.ftbguilibrary.widget;


import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;

/**
 * @author LatvianModder
 */
public abstract class SimpleTextButton extends Button
{
	public SimpleTextButton(Panel panel, ITextComponent txt, Icon icon)
	{
		super(panel, txt, icon);
		setWidth(panel.getGui().getTheme().getStringWidth(txt) + (hasIcon() ? 28 : 8));
		setHeight(20);
	}

	@Override
	public SimpleTextButton setTitle(ITextComponent txt)
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
	public void addMouseOverText(TooltipList list)
	{
		if (getGui().getTheme().getStringWidth(getTitle()) + (hasIcon() ? 28 : 8) > width)
		{
			list.add(getTitle());
		}
	}

	@Override
	public void draw(MatrixStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
		drawBackground(matrixStack, theme, x, y, w, h);
		int s = h >= 16 ? 16 : 8;
		int off = (h - s) / 2;
		ITextProperties title = getTitle();
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
			drawIcon(matrixStack, theme, x + off, y + off, s, s);
			textX += off + s;
		}

		theme.drawString(matrixStack, title, textX, textY, theme.getContentColor(getWidgetType()), Theme.SHADOW);
	}
}