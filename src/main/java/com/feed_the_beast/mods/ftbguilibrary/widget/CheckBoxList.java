package com.feed_the_beast.mods.ftbguilibrary.widget;

import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class CheckBoxList extends Button
{
	public static class CheckBoxEntry
	{
		public String name;
		public int value = 0;
		private CheckBoxList checkBoxList;

		public CheckBoxEntry(String n)
		{
			name = n;
		}

		public void onClicked(MouseButton button, int index)
		{
			select((value + 1) % checkBoxList.getValueCount());
			checkBoxList.playClickSound();
		}

		public void addMouseOverText(List<String> list)
		{
		}

		public CheckBoxEntry select(int v)
		{
			if (checkBoxList.radioButtons)
			{
				if (v > 0)
				{
					for (CheckBoxEntry entry : checkBoxList.entries)
					{
						boolean old1 = entry.value > 0;
						entry.value = 0;

						if (old1)
						{
							entry.onValueChanged();
						}
					}
				}
				else
				{
					return this;
				}
			}

			int old = value;
			value = v;

			if (old != value)
			{
				onValueChanged();
			}

			return this;
		}

		public void onValueChanged()
		{
		}
	}

	public final boolean radioButtons;
	private final List<CheckBoxEntry> entries;

	public CheckBoxList(GuiBase gui, boolean radiobutton)
	{
		super(gui);
		setSize(10, 2);
		radioButtons = radiobutton;
		entries = new ArrayList<>();
	}

	public int getValueCount()
	{
		return 2;
	}

	@Override
	public void drawBackground(MatrixStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
	}

	public void drawCheckboxBackground(MatrixStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
		theme.drawCheckboxBackground(matrixStack, x, y, w, h, radioButtons);
	}

	public void getCheckboxIcon(MatrixStack matrixStack, Theme theme, int x, int y, int w, int h, int index, int value)
	{
		theme.drawCheckbox(matrixStack, x, y, w, h, WidgetType.mouseOver(isMouseOver()), value != 0, radioButtons);
	}

	public void addBox(CheckBoxEntry checkBox)
	{
		checkBox.checkBoxList = this;
		entries.add(checkBox);
		setWidth(Math.max(width, getGui().getTheme().getStringWidth(checkBox.name)));
		setHeight(height + 11);
	}

	public CheckBoxEntry addBox(String name)
	{
		CheckBoxEntry entry = new CheckBoxEntry(name);
		addBox(entry);
		return entry;
	}

	@Override
	public void onClicked(MouseButton button)
	{
		int y = getMouseY() - getY();

		if (y % 11 == 10)
		{
			return;
		}

		int i = y / 11;

		if (i >= 0 && i < entries.size())
		{
			entries.get(i).onClicked(button, i);
		}
	}

	@Override
	public void addMouseOverText(TooltipList list)
	{
	}

	@Override
	public void draw(MatrixStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
		drawBackground(matrixStack, theme, x, y, w, h);

		for (int i = 0; i < entries.size(); i++)
		{
			CheckBoxEntry entry = entries.get(i);
			int ey = y + i * 11 + 1;
			drawCheckboxBackground(matrixStack, theme, x, ey, 10, 10);
			getCheckboxIcon(matrixStack, theme, x + 1, ey + 1, 8, 8, i, entry.value);
			theme.drawString(matrixStack, entry.name, x + 12, ey + 1);
			RenderSystem.color4f(1F, 1F, 1F, 1F);
		}
	}
}