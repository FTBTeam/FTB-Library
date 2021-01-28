package com.feed_the_beast.mods.ftbguilibrary.config.gui;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigValue;
import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.feed_the_beast.mods.ftbguilibrary.icon.MutableColor4I;
import com.feed_the_beast.mods.ftbguilibrary.utils.Bits;
import com.feed_the_beast.mods.ftbguilibrary.utils.Key;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.feed_the_beast.mods.ftbguilibrary.widget.Panel;
import com.feed_the_beast.mods.ftbguilibrary.widget.PanelScrollBar;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Theme;
import com.feed_the_beast.mods.ftbguilibrary.widget.Widget;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetLayout;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.List;

public class GuiEditConfig extends GuiBase
{
	public static final Color4I COLOR_BACKGROUND = Color4I.rgba(0x99333333);

	public static Theme THEME = new Theme()
	{
		@Override
		public void drawScrollBarBackground(PoseStack matrixStack, int x, int y, int w, int h, WidgetType type)
		{
			Color4I.BLACK.withAlpha(70).draw(matrixStack, x, y, w, h);
		}

		@Override
		public void drawScrollBar(PoseStack matrixStack, int x, int y, int w, int h, WidgetType type, boolean vertical)
		{
			getContentColor(WidgetType.NORMAL).withAlpha(100).withBorder(Color4I.GRAY.withAlpha(100), false).draw(matrixStack, x, y, w, h);
		}
	};

	public class ButtonConfigGroup extends Button
	{
		public final ConfigGroup group;
		public MutableComponent title, info;
		public boolean collapsed = false;

		public ButtonConfigGroup(Panel panel, ConfigGroup g)
		{
			super(panel);
			setHeight(12);
			group = g;

			if (group.parent != null)
			{
				List<ConfigGroup> groups = new ArrayList<>();

				g = group;

				do
				{
					groups.add(g);
					g = g.parent;
				}
				while (g != null);

				groups.remove(groups.size() - 1);

				title = new TextComponent("");

				for (int i = groups.size() - 1; i >= 0; i--)
				{
					title.append(groups.get(i).getName());

					if (i != 0)
					{
						title.append(" > ");
					}
				}
			}
			else
			{
				title = new TranslatableComponent("stat.generalButton");
			}

			String infoKey = group.getPath() + ".info";
			info = I18n.exists(infoKey) ? new TranslatableComponent(infoKey) : null;
			setCollapsed(collapsed);
		}

		public void setCollapsed(boolean v)
		{
			collapsed = v;
			setTitle(new TextComponent("").append(new TextComponent(collapsed ? "[-] " : "[v] ").withStyle(collapsed ? ChatFormatting.RED : ChatFormatting.GREEN)).append(title));
		}

		@Override
		public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h)
		{
			COLOR_BACKGROUND.draw(matrixStack, x, y, w, h);
			theme.drawString(matrixStack, getTitle(), x + 2, y + 2);
			RenderSystem.color4f(1F, 1F, 1F, 1F);

			if (isMouseOver())
			{
				Color4I.WHITE.withAlpha(33).draw(matrixStack, x, y, w, h);
			}
		}

		@Override
		public void addMouseOverText(TooltipList list)
		{
			if (info != null)
			{
				list.add(info);
			}
		}

		@Override
		public void onClicked(MouseButton button)
		{
			setCollapsed(!collapsed);
			getGui().refreshWidgets();
		}
	}

	private class ButtonConfigEntry extends Button
	{
		public final ButtonConfigGroup group;
		public final ConfigValue inst;
		public Component keyText;

		public ButtonConfigEntry(Panel panel, ButtonConfigGroup g, ConfigValue i)
		{
			super(panel);
			setHeight(12);
			group = g;
			inst = i;

			if (!inst.getCanEdit())
			{
				keyText = new TextComponent(inst.getName()).withStyle(ChatFormatting.GRAY);
			}
			else
			{
				keyText = new TextComponent(inst.getName());
			}
		}

		@Override
		public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h)
		{
			boolean mouseOver = getMouseY() >= 20 && isMouseOver();

			if (mouseOver)
			{
				Color4I.WHITE.withAlpha(33).draw(matrixStack, x, y, w, h);
			}

			theme.drawString(matrixStack, keyText, x + 4, y + 2, Bits.setFlag(0, Theme.SHADOW, mouseOver));
			RenderSystem.color4f(1F, 1F, 1F, 1F);

			FormattedText s = inst.getStringForGUI(inst.value);
			int slen = theme.getStringWidth(s);

			if (slen > 150)
			{
				s = new TextComponent(theme.trimStringToWidth(s, 150).getString().trim() + "...");
				slen = 152;
			}

			MutableColor4I textCol = inst.getColor(inst.value).mutable();
			textCol.setAlpha(255);

			if (mouseOver)
			{
				textCol.addBrightness(60);

				if (getMouseX() > x + w - slen - 9)
				{
					Color4I.WHITE.withAlpha(33).draw(matrixStack, x + w - slen - 8, y, slen + 8, h);
				}
			}

			theme.drawString(matrixStack, s, getGui().width - (slen + 20), y + 2, textCol, 0);
			RenderSystem.color4f(1F, 1F, 1F, 1F);
		}

		@Override
		public void onClicked(MouseButton button)
		{
			if (getMouseY() >= 20)
			{
				playClickSound();
				inst.onClicked(button, accepted -> run());
			}
		}

		@Override
		public void addMouseOverText(TooltipList list)
		{
			if (getMouseY() > 18)
			{
				list.add(keyText.copy().withStyle(ChatFormatting.UNDERLINE));
				String tooltip = inst.getTooltip();

				if (!tooltip.isEmpty())
				{
					for (String s : tooltip.split("\n"))
					{
						list.styledString(s, Style.EMPTY.withItalic(true).withColor(TextColor.fromLegacyFormat(ChatFormatting.GRAY)));
					}
				}

				list.blankLine();
				inst.addInfo(list);
			}
		}
	}

	private final ConfigGroup group;

	private final Component title;
	private final List<Widget> configEntryButtons;
	private final Panel configPanel;
	private final Button buttonAccept, buttonCancel, buttonCollapseAll, buttonExpandAll;
	private final PanelScrollBar scroll;
	private int groupSize = 0;

	public GuiEditConfig(ConfigGroup g)
	{
		group = g;
		title = g.getName().copy().withStyle(ChatFormatting.BOLD);
		configEntryButtons = new ArrayList<>();

		configPanel = new Panel(this)
		{
			@Override
			public void addWidgets()
			{
				for (Widget w : configEntryButtons)
				{
					if (!(w instanceof ButtonConfigEntry) || !((ButtonConfigEntry) w).group.collapsed)
					{
						add(w);
					}
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

		List<ConfigValue> list = new ArrayList<>();
		collectAllConfigValues(group, list);

		if (!list.isEmpty())
		{
			list.sort(null);

			ButtonConfigGroup group = null;

			for (ConfigValue value : list)
			{
				if (group == null || group.group != value.group)
				{
					group = new ButtonConfigGroup(configPanel, value.group);
					configEntryButtons.add(group);
					groupSize++;
				}

				configEntryButtons.add(new ButtonConfigEntry(configPanel, group, value));
			}

			if (groupSize == 1)
			{
				configEntryButtons.remove(group);
			}
		}

		scroll = new PanelScrollBar(this, configPanel);

		buttonAccept = new SimpleButton(this, new TranslatableComponent("gui.close"), GuiIcons.ACCEPT, (widget, button) -> group.save(true));
		buttonCancel = new SimpleButton(this, new TranslatableComponent("gui.cancel"), GuiIcons.CANCEL, (widget, button) -> group.save(false));

		buttonExpandAll = new SimpleButton(this, new TranslatableComponent("gui.expand_all"), GuiIcons.ADD, (widget, button) ->
		{
			for (Widget w : configEntryButtons)
			{
				if (w instanceof ButtonConfigGroup)
				{
					((ButtonConfigGroup) w).setCollapsed(false);
				}
			}

			scroll.setValue(0);
			widget.getGui().refreshWidgets();
		});

		buttonCollapseAll = new SimpleButton(this, new TranslatableComponent("gui.collapse_all"), GuiIcons.REMOVE, (widget, button) ->
		{
			for (Widget w : configEntryButtons)
			{
				if (w instanceof ButtonConfigGroup)
				{
					((ButtonConfigGroup) w).setCollapsed(true);
				}
			}

			scroll.setValue(0);
			widget.getGui().refreshWidgets();
		});
	}

	private void collectAllConfigValues(ConfigGroup group, List<ConfigValue> list)
	{
		for (ConfigValue value : group.getValues())
		{
			list.add(value);
		}

		for (ConfigGroup group1 : group.getGroups())
		{
			collectAllConfigValues(group1, list);
		}
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

		if (groupSize > 1)
		{
			add(buttonExpandAll);
			add(buttonCollapseAll);
		}

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

		if (groupSize > 1)
		{
			buttonExpandAll.setPos(width - 58, 2);
			buttonCollapseAll.setPos(width - 78, 2);
		}
	}

	@Override
	public boolean onClosedByKey(Key key)
	{
		if (super.onClosedByKey(key))
		{
			group.save(true);
			return false;
		}

		return false;
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h)
	{
		COLOR_BACKGROUND.draw(matrixStack, 0, 0, w, 20);
		theme.drawString(matrixStack, getTitle(), 6, 6, Theme.SHADOW);
	}

	@Override
	public Component getTitle()
	{
		return title;
	}

	@Override
	public Theme getTheme()
	{
		return THEME;
	}
}