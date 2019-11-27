package com.feed_the_beast.mods.ftbguilibrary.misc;

import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.PanelScrollBar;
import com.feed_the_beast.mods.ftbguilibrary.widget.TextBox;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.Widget;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetLayout;
import net.minecraft.client.resources.I18n;

/**
 * @author LatvianModder
 */
public abstract class GuiButtonListBase extends GuiBase
{
	private final Panel panelButtons;
	private final PanelScrollBar scrollBar;
	private String title = "";
	private TextBox searchBox;
	private boolean hasSearchBox;
	private int borderH, borderV, borderW;

	public GuiButtonListBase()
	{
		panelButtons = new Panel(this)
		{
			@Override
			public void add(Widget widget)
			{
				if (!hasSearchBox || searchBox.getText().isEmpty() || getFilterText(widget).contains(searchBox.getText().toLowerCase()))
				{
					super.add(widget);
				}
			}

			@Override
			public void addWidgets()
			{
				addButtons(this);
			}

			@Override
			public void alignWidgets()
			{
				setY(hasSearchBox ? 23 : 9);
				int prevWidth = width;

				if (widgets.isEmpty())
				{
					setWidth(100);
				}
				else
				{
					setWidth(100);

					for (Widget w : widgets)
					{
						setWidth(Math.max(width, w.width));
					}
				}

				if (hasSearchBox)
				{
					setWidth(Math.max(width, prevWidth));
				}

				for (Widget w : widgets)
				{
					w.setX(borderH);
					w.setWidth(width - borderH * 2);
				}

				setHeight(140);

				scrollBar.setPosAndSize(posX + width + 6, posY - 1, 16, height + 2);
				scrollBar.setMaxValue(align(new WidgetLayout.Vertical(borderV, borderW, borderV)));

				getGui().setWidth(scrollBar.posX + scrollBar.width + 8);
				getGui().setHeight(height + 18 + (hasSearchBox ? 14 : 0));

				if (hasSearchBox)
				{
					searchBox.setPosAndSize(8, 6, getGui().width - 16, 12);
				}
			}

			@Override
			public void drawBackground(Theme theme, int x, int y, int w, int h)
			{
				theme.drawPanelBackground(x, y, w, h);
			}
		};

		panelButtons.setPosAndSize(9, 9, 0, 146);

		scrollBar = new PanelScrollBar(this, panelButtons);
		scrollBar.setCanAlwaysScroll(true);
		scrollBar.setScrollStep(20);

		searchBox = new TextBox(this)
		{
			@Override
			public void onTextChanged()
			{
				panelButtons.refreshWidgets();
			}
		};

		searchBox.ghostText = I18n.format("gui.search_box");
		hasSearchBox = false;
	}

	public void setHasSearchBox(boolean v)
	{
		if (hasSearchBox != v)
		{
			hasSearchBox = v;
			refreshWidgets();
		}
	}

	public String getFilterText(Widget widget)
	{
		return widget.getTitle().toLowerCase();
	}

	@Override
	public void addWidgets()
	{
		add(panelButtons);
		add(scrollBar);

		if (hasSearchBox)
		{
			add(searchBox);
		}
	}

	@Override
	public void alignWidgets()
	{
		panelButtons.alignWidgets();
	}

	public abstract void addButtons(Panel panel);

	public void setTitle(String txt)
	{
		title = txt;
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	public void setBorder(int h, int v, int w)
	{
		borderH = h;
		borderV = v;
		borderW = w;
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		super.drawBackground(theme, x, y, w, h);

		String title = getTitle();

		if (!title.isEmpty())
		{
			theme.drawString(title, x + (width - theme.getStringWidth(title)) / 2, y - theme.getFontHeight() - 2, Theme.SHADOW);
		}
	}

	public void focus()
	{
		searchBox.setFocused(true);
	}
}