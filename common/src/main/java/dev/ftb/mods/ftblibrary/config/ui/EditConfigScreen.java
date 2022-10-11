package dev.ftb.mods.ftblibrary.config.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.PanelScrollBar;
import dev.ftb.mods.ftblibrary.ui.SimpleButton;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;
import dev.ftb.mods.ftblibrary.ui.WidgetType;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
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

public class EditConfigScreen extends BaseScreen {
	public static final Color4I COLOR_BACKGROUND = Color4I.rgba(0x99333333);

	public static Theme THEME = new Theme() {
		@Override
		public void drawScrollBarBackground(PoseStack matrixStack, int x, int y, int w, int h, WidgetType type) {
			Color4I.BLACK.withAlpha(70).draw(matrixStack, x, y, w, h);
		}

		@Override
		public void drawScrollBar(PoseStack matrixStack, int x, int y, int w, int h, WidgetType type, boolean vertical) {
			getContentColor(WidgetType.NORMAL).withAlpha(100).withBorder(Color4I.GRAY.withAlpha(100), false).draw(matrixStack, x, y, w, h);
		}
	};

	public class ConfigGroupButton extends Button {
		public final ConfigGroup group;
		public MutableComponent title, info;
		public boolean collapsed = false;

		public ConfigGroupButton(Panel panel, ConfigGroup g) {
			super(panel);
			setHeight(12);
			group = g;

			if (group.parent != null) {
				List<ConfigGroup> groups = new ArrayList<>();

				g = group;

				do {
					groups.add(g);
					g = g.parent;
				}
				while (g != null);

				groups.remove(groups.size() - 1);

				title = new TextComponent("");

				for (var i = groups.size() - 1; i >= 0; i--) {
					title.append(groups.get(i).getName());

					if (i != 0) {
						title.append(" > ");
					}
				}
			} else {
				title = new TranslatableComponent("stat.generalButton");
			}

			var infoKey = group.getPath() + ".info";
			info = I18n.exists(infoKey) ? new TranslatableComponent(infoKey) : null;
			setCollapsed(collapsed);
		}

		public void setCollapsed(boolean v) {
			collapsed = v;
			setTitle(new TextComponent("").append(new TextComponent(collapsed ? "[-] " : "[v] ").withStyle(collapsed ? ChatFormatting.RED : ChatFormatting.GREEN)).append(title));
		}

		@Override
		public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
			COLOR_BACKGROUND.draw(matrixStack, x, y, w, h);
			theme.drawString(matrixStack, getTitle(), x + 2, y + 2);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

			if (isMouseOver()) {
				Color4I.WHITE.withAlpha(33).draw(matrixStack, x, y, w, h);
			}
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			if (info != null) {
				list.add(info);
			}
		}

		@Override
		public void onClicked(MouseButton button) {
			setCollapsed(!collapsed);
			getGui().refreshWidgets();
		}
	}

	private class ConfigEntryButton extends Button {
		public final ConfigGroupButton group;
		public final ConfigValue inst;
		public Component keyText;

		public ConfigEntryButton(Panel panel, ConfigGroupButton g, ConfigValue i) {
			super(panel);
			setHeight(12);
			group = g;
			inst = i;

			if (!inst.getCanEdit()) {
				keyText = new TextComponent(inst.getName()).withStyle(ChatFormatting.GRAY);
			} else {
				keyText = new TextComponent(inst.getName());
			}
		}

		@Override
		public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
			var mouseOver = getMouseY() >= 20 && isMouseOver();

			if (mouseOver) {
				Color4I.WHITE.withAlpha(33).draw(matrixStack, x, y, w, h);
			}

			theme.drawString(matrixStack, keyText, x + 4, y + 2, Bits.setFlag(0, Theme.SHADOW, mouseOver));
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

			FormattedText s = inst.getStringForGUI(inst.value);
			var slen = theme.getStringWidth(s);

			if (slen > 150) {
				s = new TextComponent(theme.trimStringToWidth(s, 150).getString().trim() + "...");
				slen = 152;
			}

			var textCol = inst.getColor(inst.value).mutable();
			textCol.setAlpha(255);

			if (mouseOver) {
				textCol.addBrightness(60);

				if (getMouseX() > x + w - slen - 9) {
					Color4I.WHITE.withAlpha(33).draw(matrixStack, x + w - slen - 8, y, slen + 8, h);
				}
			}

			theme.drawString(matrixStack, s, getGui().width - (slen + 20), y + 2, textCol, 0);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		}

		@Override
		public void onClicked(MouseButton button) {
			if (getMouseY() >= 20) {
				playClickSound();
				inst.onClicked(button, accepted -> run());
			}
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			if (getMouseY() > 18) {
				list.add(keyText.copy().withStyle(ChatFormatting.UNDERLINE));
				var tooltip = inst.getTooltip();

				if (!tooltip.isEmpty()) {
					for (var s : tooltip.split("\n")) {
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

	public EditConfigScreen(ConfigGroup g) {
		group = g;
		title = g.getName().copy().withStyle(ChatFormatting.BOLD);
		configEntryButtons = new ArrayList<>();

		configPanel = new Panel(this) {
			@Override
			public void addWidgets() {
				for (var w : configEntryButtons) {
					if (!(w instanceof ConfigEntryButton) || !((ConfigEntryButton) w).group.collapsed) {
						add(w);
					}
				}
			}

			@Override
			public void alignWidgets() {
				for (var w : widgets) {
					w.setWidth(width - 16);
				}

				scroll.setMaxValue(align(WidgetLayout.VERTICAL));
			}
		};

		List<ConfigValue> list = new ArrayList<>();
		collectAllConfigValues(group, list);

		if (!list.isEmpty()) {
			list.sort(null);

			ConfigGroupButton group = null;

			for (var value : list) {
				if (group == null || group.group != value.group) {
					group = new ConfigGroupButton(configPanel, value.group);
					configEntryButtons.add(group);
					groupSize++;
				}

				configEntryButtons.add(new ConfigEntryButton(configPanel, group, value));
			}

			if (groupSize == 1) {
				configEntryButtons.remove(group);
			}
		}

		scroll = new PanelScrollBar(this, configPanel);

		buttonAccept = new SimpleButton(this, new TranslatableComponent("gui.accept"), Icons.ACCEPT, (widget, button) -> doAccept());
		buttonCancel = new SimpleButton(this, new TranslatableComponent("gui.cancel"), Icons.CANCEL, (widget, button) -> doCancel());

		buttonExpandAll = new SimpleButton(this, new TranslatableComponent("gui.expand_all"), Icons.ADD, (widget, button) ->
		{
			for (var w : configEntryButtons) {
				if (w instanceof ConfigGroupButton) {
					((ConfigGroupButton) w).setCollapsed(false);
				}
			}

			scroll.setValue(0);
			widget.getGui().refreshWidgets();
		});

		buttonCollapseAll = new SimpleButton(this, new TranslatableComponent("gui.collapse_all"), Icons.REMOVE, (widget, button) ->
		{
			for (var w : configEntryButtons) {
				if (w instanceof ConfigGroupButton) {
					((ConfigGroupButton) w).setCollapsed(true);
				}
			}

			scroll.setValue(0);
			widget.getGui().refreshWidgets();
		});
	}

	private void collectAllConfigValues(ConfigGroup group, List<ConfigValue> list) {
		list.addAll(group.getValues());

		for (var group1 : group.getGroups()) {
			collectAllConfigValues(group1, list);
		}
	}

	@Override
	public boolean onInit() {
		return setFullscreen();
	}

	@Override
	public void addWidgets() {
		add(buttonAccept);
		add(buttonCancel);

		if (groupSize > 1) {
			add(buttonExpandAll);
			add(buttonCollapseAll);
		}

		add(configPanel);
		add(scroll);
	}

	@Override
	public void alignWidgets() {
		configPanel.setPosAndSize(0, 20, width, height - 20);
		configPanel.alignWidgets();
		scroll.setPosAndSize(width - 16, 20, 16, height - 20);

		buttonAccept.setPos(width - 18, 2);
		buttonCancel.setPos(width - 38, 2);

		if (groupSize > 1) {
			buttonExpandAll.setPos(width - 58, 2);
			buttonCollapseAll.setPos(width - 78, 2);
		}
	}

	@Override
	public boolean keyPressed(Key key) {
		if (super.keyPressed(key)) return true;

		if (key.escOrInventory() || key.enter()) {
			if (key.esc()) {
				doCancel();
			} else {
				doAccept();
			}
			return true;
		}
		return false;
	}

	private void doAccept() {
		group.save(true);
	}

	private void doCancel() {
		group.save(false);
	}

	@Override
	public boolean onClosedByKey(Key key) {
		if (super.onClosedByKey(key)) {
			group.save(true);
			return false;
		}

		return false;
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		COLOR_BACKGROUND.draw(matrixStack, 0, 0, w, 20);
		theme.drawString(matrixStack, getTitle(), 6, 6, Theme.SHADOW);
	}

	@Override
	public Component getTitle() {
		return title;
	}

	@Override
	public Theme getTheme() {
		return THEME;
	}
}