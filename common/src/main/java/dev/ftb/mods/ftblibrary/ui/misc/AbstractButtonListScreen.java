package dev.ftb.mods.ftblibrary.ui.misc;

import com.mojang.datafixers.util.Pair;
import dev.ftb.mods.ftblibrary.ui.*;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

/**
 * Base class for a screen which displays a scrollable list of buttons, with an optional search text box.
 */
public abstract class AbstractButtonListScreen extends AbstractThreePanelScreen<AbstractButtonListScreen.ButtonPanel> {
	private Component title = Component.empty();
	private final TextBox searchBox;
	private final TextField titleField;
	private boolean hasSearchBox;
	private int borderH, borderV, borderW;

	private static final int SCROLLBAR_WIDTH = 16;
	private static final int GUTTER_SIZE = 5;

	public AbstractButtonListScreen() {
		super();

		scrollBar.setCanAlwaysScroll(true);
		scrollBar.setScrollStep(20);

		titleField = new TextField(topPanel);

		searchBox = new TextBox(topPanel) {
			@Override
			public void onTextChanged() {
				mainPanel.refreshWidgets();
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
	protected int getTopPanelHeight() {
		return hasSearchBox ?
				GUTTER_SIZE * 3 + titleField.getHeight() + searchBox.getHeight() :
				GUTTER_SIZE * 2 + titleField.getHeight();
	}

	@Override
	protected Panel createTopPanel() {
		return new ButtonListTopPanel();
	}

	@Override
	protected ButtonPanel createMainPanel() {
		return new ButtonPanel();
	}

	@Override
	protected Pair<Integer, Integer> mainPanelInset() {
		return Pair.of(2, 2);
	}

	/**
	 * Override this method to add your buttons to the panel. Just add the buttons; the panel will take care of
	 * the vertical button layout for you.
	 *
	 * @param panel the panel to add buttons to
	 */
	public abstract void addButtons(Panel panel);

	public void setTitle(Component txt) {
		title = txt;
		titleField.setText(txt);
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

	public void focus() {
		searchBox.setFocused(true);
	}

	protected class ButtonPanel extends Panel {
		public ButtonPanel() {
			super(AbstractButtonListScreen.this);
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
			align(new WidgetLayout.Vertical(borderV, borderW, borderV));

			widgets.forEach(w -> {
				w.setX(borderH);
				w.setWidth(width - borderH * 2);
			});
		}
	}

	protected class ButtonListTopPanel extends TopPanel {
		@Override
		public void addWidgets() {
			super.addWidgets();

			add(titleField);

			if (hasSearchBox) {
				add(searchBox);
			}
		}

		@Override
		public void alignWidgets() {
			super.alignWidgets();

			titleField.setPosAndSize(GUTTER_SIZE, GUTTER_SIZE, parent.width - GUTTER_SIZE * 2, titleField.getHeight());

			if (hasSearchBox) {
				searchBox.setPosAndSize(GUTTER_SIZE, titleField.getPosY() + titleField.getHeight() + GUTTER_SIZE,
						parent.width - GUTTER_SIZE * 2, getTheme().getFontHeight() + 6);
			}
		}
	}
}
