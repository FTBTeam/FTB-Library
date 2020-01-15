package com.feed_the_beast.mods.ftbguilibrary.widget;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.Bits;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.StringUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class TextField extends Widget
{
	public String[] text;
	public int textFlags = 0;
	public int maxWidth = 0;
	public int textSpacing = 10;
	public float scale = 1F;
	public Color4I textColor = Icon.EMPTY;
	private List<GuiBase.PositionedTextData> textData;

	public TextField(Panel panel)
	{
		super(panel);
	}

	public TextField addFlags(int flags)
	{
		textFlags |= flags;
		return this;
	}

	public TextField setMaxWidth(int width)
	{
		maxWidth = width;
		return this;
	}

	public TextField setColor(Color4I color)
	{
		textColor = color;
		return this;
	}

	public TextField setScale(float s)
	{
		scale = s;
		return this;
	}

	public TextField setSpacing(int s)
	{
		textSpacing = s;
		return this;
	}

	public TextField setText(String txt)
	{
		text = null;
		textData = null;
		txt = txt.trim();
		Theme theme = getGui().getTheme();

		if (!txt.isEmpty())
		{
			if (maxWidth == 0)
			{
				text = txt.split("\n");
			}
			else
			{
				text = theme.listFormattedStringToWidth(txt, maxWidth).toArray(StringUtils.EMPTY_ARRAY);
			}
		}

		if (text == null || text.length == 0)
		{
			text = StringUtils.EMPTY_ARRAY;
		}

		return resize(theme);
	}

	public TextField resize(Theme theme)
	{
		if (maxWidth == 0)
		{
			setWidth(0);

			for (String s : text)
			{
				setWidth(Math.max(width, (int) (theme.getStringWidth(s) * scale)));
			}
		}
		else
		{
			setWidth(maxWidth);
		}

		setHeight((int) ((Math.max(1, text.length) * textSpacing - (textSpacing - theme.getFontHeight() + 1)) * scale));
		return this;
	}

	public TextField setText(ITextComponent component)
	{
		setText(component.getFormattedText());

		textData = getGui().getTheme().createDataFrom(component, width);

		if (textData.isEmpty())
		{
			textData = null;
		}

		return this;
	}

	@Nullable
	private GuiBase.PositionedTextData getDataAtMouse()
	{
		if (textData == null)
		{
			return null;
		}

		int x = getX();
		int y = getY();

		for (GuiBase.PositionedTextData data : textData)
		{
			if (getGui().isMouseOver(data.posX + x, data.posY + y, data.width, data.height))
			{
				return data;
			}
		}

		return null;
	}

	@Override
	public void addMouseOverText(List<String> list)
	{
		GuiBase.PositionedTextData data = getDataAtMouse();

		if (data != null && data.hoverEvent != null) //TODO: Special handling for each data.hoverEvent.getAction()
		{
			Collections.addAll(list, data.hoverEvent.getValue().getFormattedText().split("\n"));
		}
	}

	@Override
	public boolean mousePressed(MouseButton button)
	{
		if (isMouseOver())
		{
			GuiBase.PositionedTextData data = getDataAtMouse();

			if (data != null && data.clickEvent != null && handleClick(GuiHelper.clickEventToString(data.clickEvent)))
			{
				playClickSound();
				return true;
			}
		}

		return false;
	}

	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
	}

	@Override
	public void draw(Theme theme, int x, int y, int w, int h)
	{
		drawBackground(theme, x, y, w, h);

		if (text.length == 0)
		{
			return;
		}

		boolean centered = Bits.getFlag(textFlags, Theme.CENTERED);
		boolean centeredV = Bits.getFlag(textFlags, Theme.CENTERED_V);

		Color4I col = textColor;

		if (col.isEmpty())
		{
			col = theme.getContentColor(WidgetType.mouseOver(Bits.getFlag(textFlags, Theme.MOUSE_OVER)));
		}

		int tx = x + (centered ? (w / 2) : 0);
		int ty = y + (centeredV ? ((h - theme.getFontHeight()) / 2) : 0);

		if (scale == 1F)
		{
			for (int i = 0; i < text.length; i++)
			{
				theme.drawString(text[i], tx, ty + i * textSpacing, col, textFlags);
			}
		}
		else
		{
			RenderSystem.pushMatrix();
			RenderSystem.translatef(tx, ty, 0);
			RenderSystem.scalef(scale, scale, 1F);

			for (int i = 0; i < text.length; i++)
			{
				theme.drawString(text[i], 0, i * textSpacing, col, textFlags);
			}

			RenderSystem.popMatrix();
		}
	}
}
