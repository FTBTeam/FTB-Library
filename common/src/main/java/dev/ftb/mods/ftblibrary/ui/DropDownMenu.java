package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.layout.Align;
import dev.ftb.mods.ftblibrary.ui.layout.Flex;
import dev.ftb.mods.ftblibrary.ui.layout.Insets;
import dev.ftb.mods.ftblibrary.ui.layout.LayoutHints;
import dev.ftb.mods.ftblibrary.ui.layout.StackLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Supplier;

public class DropDownMenu extends ModalPanel implements PopupMenu {
    private static final int SEARCH_HEIGHT = 12;
    private static final int SCROLLBAR_WIDTH = 14;
    private static final int LIST_TOP = 14;

    // every widget sits over the same area, placed by its hints: search box across the top, list below it, scrollbar
    // down the right hand side
    private static final WidgetLayout LAYOUT = StackLayout.builder().align(Align.START, Align.START).build();

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
        this.textBox.ghostText = Component.translatable("ftblibrary.gui.search").getString();
        boolean hasIcons = i.stream().anyMatch(item -> !item.getIcon().isEmpty());
        this.mainPanel = new MainPanel(this, i, hasIcons, textBox::getText);
        this.scrollBar = new PanelScrollBar(this, ScrollBar.Plane.VERTICAL, mainPanel);

        describeParts(textBox, mainPanel, scrollBar);
        setFitContent(true, true);
    }

    @Override
    protected WidgetLayout getLayout() {
        return LAYOUT;
    }

    static WidgetLayout layout() {
        return LAYOUT;
    }

    /** Give the three parts of the menu the size and hints that {@link #LAYOUT} places them by. */
    static void describeParts(Widget searchBox, Widget list, Widget scrollBar) {
        searchBox.setHeight(SEARCH_HEIGHT);
        searchBox.setLayoutHints(LayoutHints.DEFAULT.withHFlex(Flex.RESIZE));
        list.setLayoutHints(LayoutHints.DEFAULT.withMargin(new Insets(LIST_TOP, SCROLLBAR_WIDTH, 0, 0)));
        scrollBar.setWidth(SCROLLBAR_WIDTH);
        scrollBar.setLayoutHints(LayoutHints.DEFAULT.withHAlign(Align.END).withVFlex(Flex.RESIZE)
                .withMargin(new Insets(SEARCH_HEIGHT, 0, 0, 0)));
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
        // widgets that stretch are measured at their current size, so forget the size they had for the previous
        // contents, or a list that has become shorter would leave the menu as tall as it was
        textBox.setWidth(0);
        scrollBar.setHeight(0);
        super.alignWidgets();
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

            int wantedHeight = (int) Math.min(totalHeight, this.getWindow().getGuiScaledHeight() * (maxHeightPercent));

            // the extra space below the entries is part of the list's size
            setHeight(Math.max(wantedHeight, 32) + LIST_TOP);

            for (int i = 0; i < activeWidgets.size(); i++) {
                Widget widget = activeWidgets.get(i);
                widget.setPosAndSize(0, i * 12, widget.width, widget.height);
            }
        }
    }
}
