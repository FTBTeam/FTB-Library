package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.function.Supplier;

public class DropDownMenu extends ModalPanel implements PopupMenu {

    private final ScrollBar scrollBar;
    private final Panel mainPanel;
    private final TextBox textBox;
    private float maxHeightPercent = 0.5F;

    public DropDownMenu(Panel panel, List<ContextMenuItem> i) {
        super(panel);
        this.textBox = new TextBox(this) {
            @Override
            public void onTextChanged() {
                refreshWidgets();
                scrollBar.setValue(0);
                super.onTextChanged();
            }
        };
        this.textBox.ghostText = "Search...";
        boolean hasIcons = i.stream().anyMatch(item -> !item.getIcon().isEmpty());
        this.mainPanel = new MainPanel(this, i, hasIcons, textBox::getText);
        this.scrollBar = new PanelScrollBar(this, ScrollBar.Plane.VERTICAL, mainPanel);
    }

    @Override
    public boolean scrollPanel(double scroll) {
        return super.scrollPanel(scroll);
    }

    @Override
    public void addWidgets() {
        add(textBox);
        add(mainPanel);
        add(scrollBar);
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
        mainPanel.setPos(0, 14);
        mainPanel.alignWidgets();
        mainPanel.setHeight(mainPanel.getHeight() + 14);


        setWidth(mainPanel.width + 14);
        setHeight(mainPanel.height + 14);

        scrollBar.setPosAndSize(width - 14, 12, 14, height - 12);
        textBox.setPosAndSize(0, 0, width, 12);
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
        graphics.pose().popPose();
    }

    public void setMaxHeightPercent(float maxHeightPercent) {
        this.maxHeightPercent = maxHeightPercent;
    }

    @Override
    public ModalPanel getModalPanel() {
        return this;
    }

    public class MainPanel extends Panel {

        private final List<ContextMenuItem> items;
        private List<ContextButton> activeWidgets;
        private final boolean hasIcons;
        private final Supplier<String> filter;

        public MainPanel(Panel panel, List<ContextMenuItem> items, boolean hasIcons, Supplier<String> filter) {
            super(panel);
            this.items = items;
            this.hasIcons = hasIcons;
            this.filter = filter;
        }

        @Override
        public void addWidgets() {
            List<ContextButton> list = items.stream()
                    .filter(item -> {
                        String string = item.getTitle().getString().toLowerCase();
                        String lowerCase = filter.get().toLowerCase();
                        return string.contains(lowerCase);
                    })
                    .map(item -> new ContextButton(this, item, hasIcons))
                    .toList();
            this.activeWidgets = list;
            addAll(list);
        }

        @Override
        public void alignWidgets() {
            int totalHeight = 0;
            int maxWidth = 0;
            for (Widget widget : activeWidgets) {
                maxWidth = Math.max(maxWidth, widget.width);
                totalHeight += widget.height;
            }
            setWidth(Math.max(maxWidth + 12, 128));

            int wantedHeight = (int) Math.min(totalHeight, parent.parent.getScreen().getGuiScaledHeight() * (maxHeightPercent));

            setHeight(Math.max(wantedHeight, 32));

            for (int i = 0; i < activeWidgets.size(); i++) {
                Widget widget = activeWidgets.get(i);
                widget.setPosAndSize(0, i * 12, widget.width, widget.height);
            }
        }
    }
}
