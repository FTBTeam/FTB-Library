package dev.ftb.mods.ftblibrary.ui.misc;

import dev.ftb.mods.ftblibrary.ui.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;

/**
 * Base class for a screen which displays a scrollable list of buttons, with an optional search text box.
 *
 * @deprecated use {@link AbstractButtonListScreen}
 */
@Deprecated
public abstract class ButtonListBaseScreen extends BaseScreen {
    private static final int SCROLLBAR_WIDTH = 16;
    private static final int GUTTER_SIZE = 6;
    private final Panel buttonPanel;
    private final PanelScrollBar scrollBar;
    private final TextBox searchBox;
    private Component title = Component.empty();
    private boolean hasSearchBox;
    private int borderH, borderV, borderW;

    @Deprecated
    public ButtonListBaseScreen() {
        buttonPanel = new ButtonPanel();

        scrollBar = new PanelScrollBar(this, buttonPanel);
        scrollBar.setCanAlwaysScroll(true);
        scrollBar.setScrollStep(20);

        searchBox = new TextBox(this) {
            @Override
            public void onTextChanged() {
                buttonPanel.refreshWidgets();
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
        add(buttonPanel);
        add(scrollBar);
        if (hasSearchBox) {
            add(searchBox);
        }
    }

    @Override
    public void alignWidgets() {
        int buttonPanelWidth = getGui().width - GUTTER_SIZE * 3 - SCROLLBAR_WIDTH;
        if (hasSearchBox) {
            searchBox.setPosAndSize(GUTTER_SIZE, GUTTER_SIZE, getGui().width - GUTTER_SIZE * 2, getTheme().getFontHeight() + 2);
            buttonPanel.setPosAndSize(GUTTER_SIZE, GUTTER_SIZE * 2 + searchBox.getHeight(), buttonPanelWidth, getGui().height - searchBox.height - GUTTER_SIZE * 3);
        } else {
            buttonPanel.setPosAndSize(GUTTER_SIZE, GUTTER_SIZE, buttonPanelWidth, getGui().height - GUTTER_SIZE * 2);
        }

        buttonPanel.alignWidgets();

        scrollBar.setPosAndSize(getGui().width - GUTTER_SIZE - SCROLLBAR_WIDTH, buttonPanel.getPosY(), SCROLLBAR_WIDTH, buttonPanel.getHeight());
    }

    /**
     * Override this method to add your buttons to the panel. Just add the buttons; the panel will take care of
     * the vertical button layout for you.
     *
     * @param panel the panel to add buttons to
     */
    public abstract void addButtons(Panel panel);

    @Override
    public Component getTitle() {
        return title;
    }

    public void setTitle(Component txt) {
        title = txt;
    }

    public void setBorder(int h, int v, int w) {
        borderH = h;
        borderV = v;
        borderW = w;
    }

    @Override
    public Theme getTheme() {
        // TODO ultimately move this up to BaseScreen once theming is fully functional
        return ThemeManager.INSTANCE.getActiveTheme();
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        super.drawBackground(graphics, theme, x, y, w, h);

        var title = getTitle();

        if (title.getContents() != PlainTextContents.EMPTY) {
            theme.drawString(graphics, title, x + (width - theme.getStringWidth(title)) / 2, y - theme.getFontHeight() - 2, Theme.SHADOW);
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
            align(new WidgetLayout.Vertical(borderV, borderW, borderV));

            widgets.forEach(w -> {
                w.setX(borderH);
                w.setWidth(width - borderH * 2);
            });
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            theme.drawPanelBackground(graphics, x, y, w, h);
        }
    }
}
