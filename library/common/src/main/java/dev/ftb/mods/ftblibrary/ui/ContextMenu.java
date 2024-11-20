package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class ContextMenu extends ModalPanel implements PopupMenu {
    private static final int MARGIN = 3;

    private final List<ContextMenuItem> items;
    private final boolean hasIcons;

    private int nColumns;
    private int columnWidth;
    private int maxRows;
    private boolean drawVerticalSeparators = true;

    public ContextMenu(Panel panel, List<ContextMenuItem> i) {
        super(panel);
        items = i;

        hasIcons = items.stream().anyMatch(item -> !item.getIcon().isEmpty());
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public void setDrawVerticalSeparators(boolean drawVerticalSeparators) {
        this.drawVerticalSeparators = drawVerticalSeparators;
    }

    @Override
    public void addWidgets() {
        items.forEach(item -> add(item.createWidget(this)));
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        // close context menu if clicked outside it
        var pressed = super.mousePressed(button);

        if (!pressed && !isMouseOver()) {
            closeContextMenu();
            return true;
        }

        return pressed;
    }

    @Override
    public void alignWidgets() {
        setWidth(0);

        int totalHeight = 0;
        int maxWidth = 0;
        for (var widget : widgets) {
            maxWidth = Math.max(maxWidth, widget.width);
            totalHeight += widget.height + 1;
        }
        totalHeight += MARGIN * 2;

        // if there are too many menu items to fit vertically on-screen, use a multi-column layout
        nColumns = parent.getScreen().getGuiScaledHeight() > 0 ? (totalHeight / parent.getScreen().getGuiScaledHeight()) + 1 : 1;
        if (maxRows > 0) {
            nColumns = Math.max(nColumns, widgets.size() / maxRows);
        }
        int nRows = nColumns == 1 ? widgets.size() : (widgets.size() / (nColumns + 1)) + 1;

        columnWidth = maxWidth + MARGIN * 2;
        setWidth(columnWidth * nColumns);

        int yPos = MARGIN;
        int prevCol = 0;
        int maxHeight = 0;
        for (int i = 0; i < widgets.size(); i++) {
            int col = i / nRows;
            if (prevCol != col) {
                yPos = MARGIN;
                prevCol = col;
            }
            Widget widget = widgets.get(i);
            widget.setPosAndSize(MARGIN + columnWidth * col, yPos, maxWidth, widget.height);
            maxHeight = Math.max(maxHeight, yPos + widget.height + 1);
            yPos += widget.height + 1;
        }

        setHeight(maxHeight + MARGIN - 1);
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        theme.drawContextMenuBackground(graphics, x, y, w, h);
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        GuiHelper.setupDrawing();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 900);
        Color4I.BLACK.withAlpha(45).draw(graphics, x + 3, y + 3, w, h);
        super.draw(graphics, theme, x, y, w, h);
        if (drawVerticalSeparators) {
            for (int i = 1; i < nColumns; i++) {
                // vertical separator line between columns (only in multi-column layouts)
                Color4I.WHITE.withAlpha(130).draw(graphics, x + columnWidth * i, y + MARGIN, 1, height - MARGIN * 2);
            }
        }
        graphics.pose().popPose();
    }

    @Override
    public ModalPanel getModalPanel() {
        return this;
    }

    public static class CSeparator extends Button {
        public CSeparator(Panel panel) {
            super(panel);
            setHeight(5);
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            theme.getContentColor(WidgetType.NORMAL).withAlpha(100).draw(graphics, x + 2, y + 2, parent.width - 10, 1);
            theme.getContentColor(WidgetType.DISABLED).withAlpha(100).draw(graphics, x + 3, y + 3, parent.width - 10, 1);
        }

        @Override
        public void onClicked(MouseButton button) {
        }
    }

    public boolean hasIcons() {
        return hasIcons;
    }
}
