package dev.ftb.mods.ftblibrary.ui.misc;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import static dev.ftb.mods.ftblibrary.util.TextComponentUtils.hotkeyTooltip;

public abstract class AbstractThreePanelScreen<T extends Panel> extends BaseScreen {
    public static final Pair<Integer, Integer> NO_INSET = Pair.of(0, 0);
    protected static final int BOTTOM_PANEL_H = 25;
    private static final int SCROLLBAR_WIDTH = 12;
    protected final Panel topPanel;
    protected final T mainPanel;
    protected final Panel bottomPanel;
    protected final PanelScrollBar scrollBar;
    private boolean showBottomPanel = true;
    private boolean showCloseButton = false;
    private boolean showScrollBar = true;

    protected AbstractThreePanelScreen() {
        super();

        topPanel = createTopPanel();
        mainPanel = createMainPanel();
        bottomPanel = createBottomPanel();
        scrollBar = new PanelScrollBar(this, ScrollBar.Plane.VERTICAL, mainPanel);
    }

    @Override
    public void addWidgets() {
        add(topPanel);
        add(mainPanel);
        if (showScrollBar) {
            add(scrollBar);
        }
        if (showBottomPanel) {
            add(bottomPanel);
        }
    }

    @Override
    public void alignWidgets() {
        int topPanelHeight = getTopPanelHeight();

        topPanel.setPosAndSize(0, 0, width, topPanelHeight);
        topPanel.alignWidgets();

        var inset = mainPanelInset();
        int bottomPanelHeight = showBottomPanel ? getBottomPanelHeight() + inset.getSecond() : 0;

        mainPanel.setPosAndSize(inset.getFirst(), topPanelHeight + inset.getSecond(),
                width - inset.getFirst() * 2, height - topPanelHeight - inset.getSecond() * 2 - bottomPanelHeight);
        mainPanel.alignWidgets();

        if (showBottomPanel) {
            bottomPanel.setPosAndSize(0, height - getBottomPanelHeight(), width, getBottomPanelHeight());
            bottomPanel.alignWidgets();
        }

        if (showScrollBar) {
            scrollBar.setPosAndSize(mainPanel.getPosX() + mainPanel.getWidth() - getScrollbarWidth(), mainPanel.getPosY(), getScrollbarWidth(), mainPanel.getHeight());
        }
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
        int newWidth = (showScrollBar && scrollBar.shouldDraw() ? getGui().width - getScrollbarWidth() - 2 : getGui().width) - mainPanelInset().getFirst() * 2;
        if (prevWidth != newWidth) {
            mainPanel.setWidth(newWidth);
            mainPanel.alignWidgets();
        }
    }

    @Override
    public boolean keyPressed(Key key) {
        if (super.keyPressed(key)) {
            return true;
        } else if ((key.is(InputConstants.KEY_RETURN) || key.is(InputConstants.KEY_NUMPADENTER)) && key.modifiers.shift()) {
            this.doAccept();
            return true;
        } else {
            return false;
        }
    }

    protected abstract void doCancel();

    protected abstract void doAccept();

    protected abstract int getTopPanelHeight();

    protected abstract T createMainPanel();

    protected Pair<Integer, Integer> mainPanelInset() {
        return NO_INSET;
    }

    protected int getBottomPanelHeight() {
        return BOTTOM_PANEL_H;
    }

    protected int getScrollbarWidth() {
        return SCROLLBAR_WIDTH;
    }

    protected Panel createTopPanel() {
        return new TopPanel();
    }

    protected Panel createBottomPanel() {
        return new BottomPanel();
    }

    public void showBottomPanel(boolean show) {
        showBottomPanel = show;
    }

    public void showCloseButton(boolean show) {
        showCloseButton = show;
    }

    public void showScrollBar(boolean show) {
        showScrollBar = show;
    }

    protected class TopPanel extends Panel {
        private final SimpleButton closeButton;

        public TopPanel() {
            super(AbstractThreePanelScreen.this);

            closeButton = new SimpleButton(this, Component.translatable("gui.close"),
                    Icons.CANCEL, (btn, mb) -> doCancel());
        }

        @Override
        public void addWidgets() {
            if (showCloseButton) {
                add(closeButton);
            }
        }

        @Override
        public void alignWidgets() {
            if (showCloseButton) {
                closeButton.setPosAndSize(width - 16, 1, 14, 14);
            }
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            theme.drawPanelBackground(graphics, x, y, w, h);
            Color4I.BLACK.withAlpha(80).draw(graphics, x, y + h - 1, w, 1);
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
            theme.drawPanelBackground(graphics, x, y, w, h);
            Color4I.GRAY.withAlpha(64).draw(graphics, x, y, w, 1);
        }
    }
}
