package dev.ftb.mods.ftblibrary.ui.misc;

import com.mojang.datafixers.util.Pair;
import dev.ftb.mods.ftblibrary.ui.*;
import net.minecraft.client.gui.GuiGraphics;

import static dev.ftb.mods.ftblibrary.util.TextComponentUtils.hotkeyTooltip;

public abstract class AbstractThreePanelScreen<T extends Panel> extends BaseScreen {
    private static final int BOTTOM_PANEL_H = 25;
    private static final int SCROLLBAR_WIDTH = 12;

    public static final Pair<Integer, Integer> NO_INSET = Pair.of(0, 0);

    protected final Panel topPanel;
    protected final T mainPanel;
    protected final Panel bottomPanel;
    protected final PanelScrollBar scrollBar;

    protected AbstractThreePanelScreen() {
        super();

        topPanel = createTopPanel();
        mainPanel = createMainPanel();
        bottomPanel = new BottomPanel();
        scrollBar = new PanelScrollBar(this, ScrollBar.Plane.VERTICAL, mainPanel);
    }

    @Override
    public void addWidgets() {
        add(topPanel);
        add(mainPanel);
        add(scrollBar);
        add(bottomPanel);
    }

    @Override
    public void alignWidgets() {
        int topPanelHeight = getTopPanelHeight();

        topPanel.setPosAndSize(0, 0, width, topPanelHeight);
        topPanel.alignWidgets();

        var inset = mainPanelInset();
        mainPanel.setPosAndSize(inset.getFirst(), topPanelHeight + inset.getSecond(), width - inset.getFirst() * 2, height - BOTTOM_PANEL_H - topPanelHeight - inset.getSecond() * 2);
        mainPanel.alignWidgets();

        bottomPanel.setPosAndSize(0, height - BOTTOM_PANEL_H, width, BOTTOM_PANEL_H);
        bottomPanel.alignWidgets();

        scrollBar.setPosAndSize(mainPanel.getPosX() + mainPanel.getWidth() - getScrollbarWidth(), mainPanel.getPosY(), getScrollbarWidth(), mainPanel.getHeight());
    }

    @Override
    public Theme getTheme() {
        // TODO ultimately move this up to BaseScreen once theming is fully functional
        return ThemeManager.INSTANCE.getActiveTheme();
    }

    @Override
    public void tick() {
        super.tick();

        int prevWidth = mainPanel.width;
        int newWidth = (scrollBar.shouldDraw() ? getGui().width - getScrollbarWidth() - 2 : getGui().width) - mainPanelInset().getFirst() * 2;
        if (prevWidth != newWidth) {
            mainPanel.setWidth(newWidth);
            mainPanel.alignWidgets();
        }
    }

    protected abstract void doCancel();

    protected abstract void doAccept();

    protected abstract int getTopPanelHeight();

    protected abstract T createMainPanel();

    protected Pair<Integer,Integer> mainPanelInset() {
        return NO_INSET;
    }

    protected int getScrollbarWidth() {
        return SCROLLBAR_WIDTH;
    }

    protected Panel createTopPanel() {
        return new TopPanel();
    }

    protected class TopPanel extends Panel {
        public TopPanel() {
            super(AbstractThreePanelScreen.this);
        }

        @Override
        public void addWidgets() {
        }

        @Override
        public void alignWidgets() {
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            theme.drawWidget(graphics, x, y, w, h, WidgetType.NORMAL);
        }
    }

    private class BottomPanel extends Panel {
        private final Button buttonAccept, buttonCancel;

        public BottomPanel() {
            super(AbstractThreePanelScreen.this);

            buttonAccept = SimpleTextButton.accept(this, mb -> doAccept(), hotkeyTooltip("⇧ + Enter"));
            buttonCancel = SimpleTextButton.cancel(this, mb -> doCancel(), hotkeyTooltip("ESC"));
        }

        @Override
        public void addWidgets() {
            add(buttonAccept);
            add(buttonCancel);
        }

        @Override
        public void alignWidgets() {
            buttonCancel.setPos(width - buttonCancel.width - 5, 2);
            buttonAccept.setPos(buttonCancel.posX - buttonAccept.width - 5, 2);
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            theme.drawWidget(graphics, x, y, w, h, WidgetType.NORMAL);
        }
    }
}
