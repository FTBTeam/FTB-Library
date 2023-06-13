package dev.ftb.mods.ftblibrary.config.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.ConfigValue;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;

public class EditConfigScreen extends BaseScreen {
	public static final Color4I COLOR_BACKGROUND = Color4I.rgba(0x99333333);

	public static Theme THEME = new Theme() {
		@Override
		public void drawScrollBarBackground(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type) {
			Color4I.BLACK.withAlpha(70).draw(graphics, x, y, w, h);
		}

		@Override
		public void drawScrollBar(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type, boolean vertical) {
			getContentColor(WidgetType.NORMAL).withAlpha(100).withBorder(Color4I.GRAY.withAlpha(100), false).draw(graphics, x, y, w, h);
		}
	};

	private final ConfigGroup group;
	private final Component title;
	private final List<Widget> allConfigButtons; // both groups and entries
	private final Panel configPanel;
	private final Button buttonAccept, buttonCancel, buttonCollapseAll, buttonExpandAll;
	private final PanelScrollBar scroll;

	private int groupSize = 0;
	private boolean autoclose = false;
	private int dividerX;

	public EditConfigScreen(ConfigGroup configGroup) {
		group = configGroup;
		title = configGroup.getName().copy().withStyle(ChatFormatting.BOLD);
		allConfigButtons = new ArrayList<>();

		configPanel = new ConfigPanel();

		List<ConfigValue<?>> list = new ArrayList<>();
		collectAllConfigValues(group, list);

		if (!list.isEmpty()) {
			list.sort(null);

			ConfigGroupButton group = null;

			for (var value : list) {
				if (group == null || group.group != value.getGroup()) {
					allConfigButtons.add(new VerticalSpaceWidget(configPanel, 4));
					group = new ConfigGroupButton(configPanel, value.getGroup());
					allConfigButtons.add(group);
					groupSize++;
				}

				ConfigEntryButton<?> btn = new ConfigEntryButton<>(configPanel, group, value);
				allConfigButtons.add(btn);
				dividerX = Math.max(dividerX, getTheme().getStringWidth(btn.keyText));
			}

			if (groupSize == 1) {
				allConfigButtons.remove(group);
			}
		}
		dividerX += 10;

		scroll = new PanelScrollBar(this, configPanel);

		buttonAccept = new SimpleButton(this, Component.translatable("gui.accept"), Icons.ACCEPT,
				(widget, button) -> doAccept());
		buttonCancel = new SimpleButton(this, Component.translatable("gui.cancel"), Icons.CANCEL,
				(widget, button) -> doCancel());
		buttonExpandAll = new SimpleButton(this, Component.translatable("gui.expand_all"), Icons.ADD,
				(widget, button) -> toggleAll(false));
		buttonCollapseAll = new SimpleButton(this, Component.translatable("gui.collapse_all"), Icons.REMOVE,
				(widget, button) -> toggleAll(true));
	}

	private void toggleAll(boolean collapsed) {
		for (var w : allConfigButtons) {
			if (w instanceof ConfigGroupButton cgb) {
				cgb.setCollapsed(collapsed);
			}
		}

		scroll.setValue(0);
		getGui().refreshWidgets();
	}

	private void collectAllConfigValues(ConfigGroup group, List<ConfigValue<?>> list) {
		list.addAll(group.getValues());

		for (var subgroup : group.getSubgroups()) {
			collectAllConfigValues(subgroup, list);
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

	/**
	 * Set auto-close behaviour when Accept or Cancel buttons are clicked
	 * @param autoclose true to close the config screen, false if the config group's save-callback should handle it
	 */
	public EditConfigScreen setAutoclose(boolean autoclose) {
		this.autoclose = autoclose;
		return this;
	}

	private void doAccept() {
		group.save(true);
		if (autoclose) closeGui();
	}

	private void doCancel() {
		group.save(false);
		if (autoclose) closeGui();
	}

	@Override
	public boolean onClosedByKey(Key key) {
		if (super.onClosedByKey(key)) {
			group.save(true);
			return true;
		}

		return false;
	}

	@Override
	public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		COLOR_BACKGROUND.draw(graphics, 0, 0, w, 20);
		theme.drawString(graphics, getTitle(), 6, 6, Theme.SHADOW);
	}

	@Override
	public Component getTitle() {
		return title;
	}

	@Override
	public Theme getTheme() {
		return THEME;
	}

	public static class ConfigGroupButton extends Button {
		public final ConfigGroup group;
		public MutableComponent title, info;
		public boolean collapsed = false;

		public ConfigGroupButton(Panel panel, ConfigGroup g) {
			super(panel);
			setHeight(12);
			group = g;

			if (group.getParent() != null) {
				List<ConfigGroup> groups = new ArrayList<>();
				do {
					groups.add(g);
					g = g.getParent();
				} while (g != null);
				groups.remove(groups.size() - 1);

				title = Component.literal("");

				for (var i = groups.size() - 1; i >= 0; i--) {
					title.append(groups.get(i).getName());

					if (i != 0) {
						title.append(" > ");
					}
				}
			} else {
				title = Component.translatable("stat.generalButton");
			}
			title.withStyle(ChatFormatting.YELLOW);

			var infoKey = group.getPath() + ".info";
			info = I18n.exists(infoKey) ? Component.translatable(infoKey) : null;
			setCollapsed(collapsed);
		}

		public void setCollapsed(boolean collapsed) {
			this.collapsed = collapsed;
			setTitle(Component.literal(this.collapsed ? "[-] " : "[v] ").withStyle(this.collapsed ? ChatFormatting.RED : ChatFormatting.GREEN).append(title));
		}

		@Override
		public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
			COLOR_BACKGROUND.draw(graphics, x, y, w, h);
			theme.drawString(graphics, getTitle(), x + 3, y + 2);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

			Color4I.GRAY.withAlpha(80).draw(graphics, 0, y, width, 1);
			Color4I.GRAY.withAlpha(80).draw(graphics, 0, y, 1, height);
			if (isMouseOver()) {
				Color4I.WHITE.withAlpha(33).draw(graphics, x, y, w, h);
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

	private class ConfigEntryButton<T> extends Button {
		private final ConfigGroupButton groupButton;
		private final ConfigValue<T> configValue;
		private final Component keyText;

		public ConfigEntryButton(Panel panel, ConfigGroupButton groupButton, ConfigValue<T> configValue) {
			super(panel);
			setHeight(getTheme().getFontHeight() + 2);
			this.groupButton = groupButton;
			this.configValue = configValue;

			keyText = this.configValue.getCanEdit() ?
					Component.literal(this.configValue.getName()) :
					Component.literal(this.configValue.getName()).withStyle(ChatFormatting.GRAY);
		}

		@Override
		public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
			var mouseOver = getMouseY() >= 20 && isMouseOver();

			if (mouseOver) {
				Color4I.WHITE.withAlpha(33).draw(graphics, x, y, w, h);
			}

			theme.drawString(graphics, keyText, 5, y + 2, Bits.setFlag(0, Theme.SHADOW, mouseOver));
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

			Component s = configValue.getStringForGUI(configValue.getValue());
			var slen = theme.getStringWidth(s);

			int maxLen = width - dividerX - 10;
			if (slen > maxLen) {
				s = Component.literal(theme.trimStringToWidth(s, maxLen).getString().trim() + "...");
				slen = maxLen + 2;
			}

			var textCol = configValue.getColor().mutable();
			textCol.setAlpha(255);

			if (mouseOver) {
				textCol.addBrightness(60);
				if (getMouseX() > x + w - slen - 9) {
					Color4I.WHITE.withAlpha(33).draw(graphics, x + w - slen - 8, y, slen + 8, h);
				}
			}

			theme.drawString(graphics, s, dividerX + 5, y + 2, textCol, 0);

			Color4I.GRAY.withAlpha(33).draw(graphics, dividerX, y, 1, height);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		}

		@Override
		public void onClicked(MouseButton button) {
			if (getMouseY() >= 20) {
				playClickSound();
				configValue.onClicked(button, accepted -> run());
			}
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			if (getMouseY() > 18) {
				list.add(keyText.copy().withStyle(ChatFormatting.UNDERLINE));
				var tooltip = configValue.getTooltip();

				if (!tooltip.isEmpty()) {
					for (var s : tooltip.split("\n")) {
						list.styledString(s, Style.EMPTY.withItalic(true).withColor(TextColor.fromLegacyFormat(ChatFormatting.GRAY)));
					}
				}

				list.blankLine();
				configValue.addInfo(list);
			}
		}
	}

	private class ConfigPanel extends Panel {
		public ConfigPanel() {
			super(EditConfigScreen.this);
		}

		@Override
		public void addWidgets() {
			for (var w : allConfigButtons) {
				if (!(w instanceof ConfigEntryButton<?> cgb) || !cgb.groupButton.collapsed) {
					add(w);
				}
			}
		}

		@Override
		public void alignWidgets() {
			widgets.forEach(w -> w.setWidth(width - 16));
			align(WidgetLayout.VERTICAL);
		}
	}
}
