package com.feed_the_beast.mods.ftbguilibrary.config.gui;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigList;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigValue;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.MutableColor4I;
import com.feed_the_beast.mods.ftbguilibrary.utils.Key;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.PanelScrollBar;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.Widget;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetLayout;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * @author LatvianModder
 */
public class GuiEditConfigList<E, CV extends ConfigValue<E>> extends GuiBase
{
	public static class ButtonConfigValue<E, CV extends ConfigValue<E>> extends Button
	{
		public final ConfigList<E, CV> list;
		public final int index;

		public ButtonConfigValue(Panel panel, ConfigList<E, CV> l, int i)
		{
			super(panel);
			list = l;
			index = i;
			setHeight(12);
		}

		@Override
		public void draw(Theme theme, int x, int y, int w, int h)
		{
			boolean mouseOver = getMouseY() >= 20 && isMouseOver();

			MutableColor4I textCol = list.type.getColor(list.current.get(index)).mutable();
			textCol.setAlpha(255);

			if (mouseOver)
			{
				textCol.addBrightness(60);

				Color4I.WHITE.withAlpha(33).draw(x, y, w, h);

				if (getMouseX() >= x + w - 19)
				{
					Color4I.WHITE.withAlpha(33).draw(x + w - 19, y, 19, h);
				}
			}

			theme.drawString(getGui().getTheme().trimStringToWidth(list.type.getStringForGUI(list.current.get(index)), width), x + 4, y + 2, textCol, 0);

			if (mouseOver)
			{
				theme.drawString("[-]", x + w - 16, y + 2, Color4I.WHITE, 0);
			}

			GlStateManager.color4f(1F, 1F, 1F, 1F);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			playClickSound();

			if (getMouseX() >= getX() + width - 19)
			{
				if (list.canEdit)
				{
					list.current.remove(index);
					parent.refreshWidgets();
				}
			}
			else
			{
				list.type.current = list.current.get(index);
				list.type.initial = list.type.copy(list.type.current);
				list.type.onClicked(button, () -> {
					list.current.set(index, list.type.current);
					getGui().openGui();
				});
			}
		}

		@Override
		public void addMouseOverText(List<String> l)
		{
			if (getMouseX() >= getX() + width - 19)
			{
				l.add(I18n.format("selectServer.delete"));
			}
			else
			{
				list.type.current = list.current.get(index);
				list.type.addInfo(l);
			}
		}
	}

	public class ButtonAddValue extends Button
	{
		public ButtonAddValue(Panel panel)
		{
			super(panel);
			setHeight(12);
			setTitle("+ " + I18n.format("gui.add"));
		}

		@Override
		public void draw(Theme theme, int x, int y, int w, int h)
		{
			boolean mouseOver = getMouseY() >= 20 && isMouseOver();

			if (mouseOver)
			{
				Color4I.WHITE.withAlpha(33).draw(x, y, w, h);
			}

			theme.drawString(getTitle(), x + 4, y + 2, theme.getContentColor(getWidgetType()), Theme.SHADOW);
			GlStateManager.color4f(1F, 1F, 1F, 1F);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			playClickSound();
			list.type.current = list.type.copy(list.type.defaultValue);
			list.current.add(list.type.current);
			list.type.initial = list.type.copy(list.type.current);
			list.type.onClicked(button, () -> {
				list.current.set(list.current.size() - 1, list.type.current);
				getGui().openGui();
			});
		}

		@Override
		public void addMouseOverText(List<String> list)
		{
		}
	}

	private final ConfigList<E, CV> list;
	private final Runnable callback;

	private final String title;
	private final Panel configPanel;
	private final Button buttonAccept, buttonCancel;
	private final PanelScrollBar scroll;

	public GuiEditConfigList(ConfigList<E, CV> l, Runnable cb)
	{
		list = l;
		callback = cb;

		title = TextFormatting.BOLD + list.getName();

		configPanel = new Panel(this)
		{
			@Override
			public void addWidgets()
			{
				for (int i = 0; i < list.current.size(); i++)
				{
					add(new ButtonConfigValue<>(this, list, i));
				}

				if (list.canEdit)
				{
					add(new ButtonAddValue(this));
				}
			}

			@Override
			public void alignWidgets()
			{
				for (Widget w : widgets)
				{
					w.setWidth(width - 16);
				}

				scroll.setMaxValue(align(WidgetLayout.VERTICAL));
			}
		};

		scroll = new PanelScrollBar(this, configPanel);

		buttonAccept = new SimpleButton(this, I18n.format("gui.accept"), GuiIcons.ACCEPT, (widget, button) ->
		{
			widget.getGui().closeGui();
			callback.run();
		});

		buttonCancel = new SimpleButton(this, I18n.format("gui.cancel"), GuiIcons.CANCEL, (widget, button) -> widget.getGui().closeGui());
	}

	@Override
	public boolean onInit()
	{
		return setFullscreen();
	}

	@Override
	public void addWidgets()
	{
		add(buttonAccept);
		add(buttonCancel);
		add(configPanel);
		add(scroll);
	}

	@Override
	public void alignWidgets()
	{
		configPanel.setPosAndSize(0, 20, width, height - 20);
		configPanel.alignWidgets();
		scroll.setPosAndSize(width - 16, 20, 16, height - 20);

		buttonAccept.setPos(width - 18, 2);
		buttonCancel.setPos(width - 38, 2);
	}

	@Override
	public boolean onClosedByKey(Key key)
	{
		if (super.onClosedByKey(key))
		{
			buttonCancel.onClicked(MouseButton.LEFT);
		}

		return false;
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		GuiEditConfig.COLOR_BACKGROUND.draw(0, 0, w, 20);
		theme.drawString(getTitle(), 6, 6, Theme.SHADOW);
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	@Override
	public Theme getTheme()
	{
		return GuiEditConfig.THEME;
	}
}