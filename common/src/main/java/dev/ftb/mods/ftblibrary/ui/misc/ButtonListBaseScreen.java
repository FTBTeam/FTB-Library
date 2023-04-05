package dev.ftb.mods.ftblibrary.ui.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.ui.*;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;

/**
 * @author LatvianModder
 */
public abstract class ButtonListBaseScreen extends BaseScreen {
	private final Panel panelButtons;
	private final PanelScrollBar scrollBar;
	private Component title = Component.empty();
	private final TextBox searchBox;
	private boolean hasSearchBox;
	private int borderH, borderV, borderW;

	public ButtonListBaseScreen() {
		panelButtons = new ButtonPanel();
		panelButtons.setPosAndSize(9, 9, 0, 146);

		scrollBar = new PanelScrollBar(this, panelButtons);
		scrollBar.setCanAlwaysScroll(true);
		scrollBar.setScrollStep(20);

		searchBox = new TextBox(this) {
			@Override
			public void onTextChanged() {
				panelButtons.refreshWidgets();
			}
		};
		searchBox.ghostText = I18n.get("gui.search_box");
		hasSearchBox = false;
	}

	public void setHasSearchBox(boolean newVal) {
		if (hasSearchBox != newVal) {
			hasSearchBox = newVal;
			refreshWidgets();
		}
	}

	public String getFilterText(Widget widget) {
		return widget.getTitle().getString().toLowerCase();
	}

	@Override
	public void addWidgets() {
		add(panelButtons);
		add(scrollBar);

		if (hasSearchBox) {
			add(searchBox);
		}
	}

	@Override
	public void alignWidgets() {
		panelButtons.alignWidgets();
	}

	/**
	 * Override this method to add your button to the panel. Just add the buttons; the panel will take care of
	 * button layout for you.
	 *
	 * @param panel the panel to add buttons to
	 */
	public abstract void addButtons(Panel panel);

	public void setTitle(Component txt) {
		title = txt;
	}

	@Override
	public Component getTitle() {
		return title;
	}

	public void setBorder(int h, int v, int w) {
		borderH = h;
		borderV = v;
		borderW = w;
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		super.drawBackground(matrixStack, theme, x, y, w, h);

		var title = getTitle();

		if (title.getContents() != ComponentContents.EMPTY) {
			theme.drawString(matrixStack, title, x + (width - theme.getStringWidth(title)) / 2, y - theme.getFontHeight() - 2, Theme.SHADOW);
		}
	}

	public void focus() {
		searchBox.setFocused(true);
	}

	private class ButtonPanel extends Panel {
		public ButtonPanel() {
			super(ButtonListBaseScreen.this);
		}

		@Override
		public void add(Widget widget) {
			if (!hasSearchBox || searchBox.getText().isEmpty() || getFilterText(widget).contains(searchBox.getText().toLowerCase())) {
				super.add(widget);
			}
		}

		@Override
		public void addWidgets() {
			addButtons(this);
		}

		@Override
		public void alignWidgets() {
			setY(hasSearchBox ? 23 : 9);
			var prevWidth = width;

			if (widgets.isEmpty()) {
				setWidth(100);
			} else {
				setWidth(100);

				for (var w : widgets) {
					setWidth(Math.max(width, w.width));
				}
			}

			if (width > ButtonListBaseScreen.this.width - 40) {
				width = ButtonListBaseScreen.this.width - 40;
			}

			if (hasSearchBox) {
				setWidth(Math.max(width, prevWidth));
			}

			for (var w : widgets) {
				w.setX(borderH);
				w.setWidth(width - borderH * 2);
			}

			setHeight(parent.height - 26);

			scrollBar.setPosAndSize(posX + width + 6, posY - 1, 16, height + 2);
			scrollBar.setMaxValue(align(new WidgetLayout.Vertical(borderV, borderW, borderV)));

			getGui().setWidth(scrollBar.posX + scrollBar.width + 8);
			getGui().setHeight(height + 18 + (hasSearchBox ? 14 : 0));

			if (hasSearchBox) {
				searchBox.setPosAndSize(8, 6, getGui().width - 16, 12);
			}
		}

		@Override
		public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
			theme.drawPanelBackground(matrixStack, x, y, w, h);
		}
	}
}
