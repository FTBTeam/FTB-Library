package dev.ftb.mods.ftblibrary.ui.misc;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.layout.Align;
import dev.ftb.mods.ftblibrary.ui.layout.Flex;
import dev.ftb.mods.ftblibrary.ui.layout.GridLayout.Track;
import dev.ftb.mods.ftblibrary.ui.layout.Insets;
import dev.ftb.mods.ftblibrary.ui.layout.LayoutHints;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Developer screen showing what the layouts in {@link dev.ftb.mods.ftblibrary.ui.layout} can do. Every demo is a small
 * panel that lays out coloured boxes; each box shows its label and its current size, so you can see how space is
 * shared. Resize the game window (or reopen the screen) to watch weighted and filling widgets follow. The screen itself
 * is built from the same layouts it demonstrates.
 * <p>
 * Open it with {@code /ftblibrary test_layout_screen} (dev mode only).
 */
public class LayoutTestScreen extends BaseScreen {
    private static final int[] PALETTE = {0xE06C75, 0x98C379, 0xE5C07B, 0x61AFEF, 0xC678DD, 0x56B6C2, 0xD19A66, 0xABB2BF};
    private static final int TAB_HEIGHT = 20;
    private static final int[] FLOW_WIDTHS = {24, 36, 20, 44, 28, 32, 22, 40, 26, 34, 30, 38};
    private static final int[] FLOW_HEIGHTS = {12, 18, 12, 16, 12, 20, 12, 14, 18, 12, 16, 12};

    private static final WidgetLayout SCREEN_LAYOUT = WidgetLayout.vertical().padding(4).spacing(4).fillCross().build();
    private static final WidgetLayout CELL_LAYOUT = WidgetLayout.vertical().spacing(2).fillCross().build();

    private enum Page {
        LINEAR("Linear"), STACK("Stack"), FLOW("Flow"), GRID("Grid");

        private final String label;

        Page(String label) {
            this.label = label;
        }
    }

    private Page page = Page.LINEAR;
    private final TabBar tabBar = new TabBar(this);
    private final ContentPanel content = new ContentPanel(this);

    @Override
    public boolean onInit() {
        return setSizeProportional(0.9f, 0.9f);
    }

    @Override
    public void addWidgets() {
        content.setLayoutHints(LayoutHints.DEFAULT.withVFlex(Flex.RESIZE));
        add(tabBar);
        add(content);
    }

    @Override
    protected WidgetLayout getLayout() {
        // tab bar keeps its own height; the content takes everything else
        return SCREEN_LAYOUT;
    }

    private void select(Page newPage) {
        page = newPage;
        tabBar.refreshWidgets();
        content.refreshWidgets();
    }

    // ---- drawing helpers (the only places that touch the graphics API directly)

    private static void fill(GuiGraphics graphics, Color4I color, int x, int y, int w, int h) {
        color.draw(graphics, x, y, w, h);
    }

    private static void outline(GuiGraphics graphics, Color4I color, int x, int y, int w, int h) {
        GuiHelper.drawHollowRect(graphics, x, y, w, h, color, false);
    }

    // ---- building blocks

    /** A coloured box that shows its label and current size. */
    private static class Swatch extends Widget {
        private final Color4I color;
        private final String label;

        Swatch(Panel parent, int w, int h, int colorIndex, String label) {
            super(parent);
            this.color = Color4I.rgb(PALETTE[colorIndex % PALETTE.length]);
            this.label = label;
            setSize(w, h);
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            fill(graphics, color, x, y, w, h);
            outline(graphics, color.addBrightness(-0.3f), x, y, w, h);

            String sized = label + " " + w + "x" + h;
            String text = theme.getStringWidth(sized) + 2 <= w ? sized : label;
            int textWidth = theme.getStringWidth(text);
            if (textWidth + 2 <= w && theme.getFontHeight() <= h) {
                theme.drawString(graphics, text, x + (w - textWidth) / 2, y + (h - theme.getFontHeight()) / 2 + 1, Color4I.BLACK, 0);
            }
        }
    }

    /** The dark area of a demo, laid out by whichever layout the demo is about. */
    private static class DemoPanel extends Panel {
        private final WidgetLayout layout;
        private final Consumer<DemoPanel> filler;

        DemoPanel(Panel parent, WidgetLayout layout, Consumer<DemoPanel> filler) {
            super(parent);
            this.layout = layout;
            this.filler = filler;
        }

        @Override
        protected WidgetLayout getLayout() {
            return layout;
        }

        @Override
        public void addWidgets() {
            filler.accept(this);
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            fill(graphics, Color4I.rgb(0x1E2227), x, y, w, h);
            outline(graphics, Color4I.rgb(0x5C6370), x, y, w, h);
        }

        /** Add a box, coloured by its position, with optional hints. */
        Swatch box(int w, int h, String label, UnaryOperator<LayoutHints> hints) {
            Swatch swatch = new Swatch(this, w, h, getWidgets().size(), label);
            swatch.setLayoutHints(hints.apply(LayoutHints.DEFAULT));
            add(swatch);
            return swatch;
        }

        Swatch box(int w, int h, String label) {
            return box(w, h, label, UnaryOperator.identity());
        }
    }

    /** A titled demo: name, the code that builds its layout, and the demo area that fills the rest. */
    private static class Cell extends Panel {
        private final String title;
        private final String code;
        private final WidgetLayout demoLayout;
        private final Consumer<DemoPanel> filler;

        Cell(Panel parent, String title, String code, WidgetLayout demoLayout, Consumer<DemoPanel> filler) {
            super(parent);
            this.title = title;
            this.code = code;
            this.demoLayout = demoLayout;
            this.filler = filler;
            setLayoutHints(LayoutHints.DEFAULT.resize());
        }

        @Override
        protected WidgetLayout getLayout() {
            return CELL_LAYOUT;
        }

        @Override
        public void addWidgets() {
            add(new TextField(this).setText(Component.literal(title).withStyle(ChatFormatting.BOLD)));

            // the code wraps to the width of the cell, and the demo below takes whatever height is left
            TextField codeText = new TextField(this).setWrapToWidth();
            codeText.setColor(Color4I.GRAY);
            codeText.setLayoutHints(LayoutHints.DEFAULT.withHFlex(Flex.RESIZE));
            codeText.setText(code);
            add(codeText);

            DemoPanel demo = new DemoPanel(this, demoLayout, filler);
            demo.setLayoutHints(LayoutHints.DEFAULT.withVFlex(Flex.RESIZE));
            add(demo);
        }
    }

    /** Page tabs on the left, a hint on the right, with the space in between shared out by a resizing spacer. */
    private class TabBar extends Panel {
        TabBar(Panel parent) {
            super(parent);
            setHeight(TAB_HEIGHT);
        }

        @Override
        protected WidgetLayout getLayout() {
            return WidgetLayout.horizontal().spacing(2).crossAlign(Align.CENTER).build();
        }

        @Override
        public void addWidgets() {
            for (Page p : Page.values()) {
                add(SimpleTextButton.create(this, Component.literal(p == page ? "> " + p.label : p.label), Icon.empty(),
                        mb -> select(p)));
            }

            Widget spacer = new Widget(this);
            spacer.setSize(1, 1);
            spacer.setLayoutHints(LayoutHints.DEFAULT.withHFlex(Flex.RESIZE));
            add(spacer);

            add(new TextField(this).setColor(Color4I.GRAY).setText("Esc to close"));
        }
    }

    /** Holds the cells of the current page in a grid of equal-weight tracks. */
    private class ContentPanel extends Panel {
        ContentPanel(Panel parent) {
            super(parent);
        }

        @Override
        protected WidgetLayout getLayout() {
            return switch (page) {
                case LINEAR, GRID -> WidgetLayout.grid()
                        .columns(Track.weight(1), Track.weight(1))
                        .rows(Track.weight(1), Track.weight(1))
                        .spacing(4).build();
                case STACK -> WidgetLayout.grid()
                        .columns(Track.weight(1), Track.weight(1))
                        .rows(Track.weight(1))
                        .spacing(4).build();
                case FLOW -> WidgetLayout.grid()
                        .columns(Track.weight(1), Track.weight(1), Track.weight(1))
                        .rows(Track.weight(1))
                        .spacing(4).build();
            };
        }

        @Override
        public void addWidgets() {
            switch (page) {
                case LINEAR -> addLinearCells();
                case STACK -> addStackCells();
                case FLOW -> addFlowCells();
                case GRID -> addGridCells();
            }
        }

        private void cell(String title, String code, WidgetLayout layout, Consumer<DemoPanel> filler) {
            add(new Cell(this, title, code, layout, filler));
        }

        private void addLinearCells() {
            cell("Column, fill cross axis",
                    "vertical().padding(4).spacing(3).fillCross()",
                    WidgetLayout.vertical().padding(4).spacing(3).fillCross().build(),
                    p -> {
                        p.box(20, 12, "A");
                        p.box(40, 16, "B");
                        p.box(30, 12, "C");
                    });

            cell("Column, cross alignment",
                    "vertical().crossAlign(CENTER) + per-widget hints",
                    WidgetLayout.vertical().padding(4).spacing(3).crossAlign(Align.CENTER).build(),
                    p -> {
                        p.box(40, 12, "start", h -> h.withHAlign(Align.START));
                        p.box(40, 12, "inherit");
                        p.box(40, 12, "end", h -> h.withHAlign(Align.END));
                        p.box(40, 12, "fill", h -> h.withHFlex(Flex.RESIZE));
                    });

            cell("Row, weighted resize",
                    "horizontal().fillCross() + RESIZE weights 1 and 2",
                    WidgetLayout.horizontal().padding(4).spacing(3).fillCross().build(),
                    p -> {
                        p.box(30, 12, "fixed");
                        p.box(10, 12, "w1", h -> h.withHFlex(Flex.RESIZE).withWeight(1));
                        p.box(10, 12, "w2", h -> h.withHFlex(Flex.RESIZE).withWeight(2));
                        p.box(30, 12, "fixed");
                    });

            cell("Column, margins and limits",
                    "RESIZE + maxSize / minSize / margin",
                    WidgetLayout.vertical().padding(4).spacing(2).fillCross().build(),
                    p -> {
                        p.box(20, 12, "fixed");
                        p.box(20, 8, "max 30", h -> h.withVFlex(Flex.RESIZE).withMaxSize(LayoutHints.UNBOUNDED, 30));
                        p.box(20, 8, "min 24", h -> h.withVFlex(Flex.RESIZE).withMinSize(0, 24).withMargin(Insets.of(0, 8)));
                        p.box(20, 8, "rest", h -> h.withVFlex(Flex.RESIZE));
                    });
        }

        private void addStackCells() {
            cell("Anchoring",
                    "stack() + hAlign / vAlign / margin; RESIZE fills",
                    WidgetLayout.stack().padding(4).build(),
                    p -> {
                        p.box(10, 10, "bg", h -> h.resize());
                        Insets m = Insets.of(4);
                        p.box(30, 14, "TL", h -> h.withMargin(m));
                        p.box(30, 14, "TR", h -> h.withMargin(m).withHAlign(Align.END));
                        p.box(30, 14, "BL", h -> h.withMargin(m).withVAlign(Align.END));
                        p.box(30, 14, "BR", h -> h.withMargin(m).withHAlign(Align.END).withVAlign(Align.END));
                        p.box(36, 18, "mid", h -> h.withHAlign(Align.CENTER).withVAlign(Align.CENTER));
                    });

            cell("Default alignment",
                    "stack().align(CENTER, END)",
                    WidgetLayout.stack().padding(4).align(Align.CENTER, Align.END).build(),
                    p -> {
                        p.box(10, 14, "bar", h -> h.withHFlex(Flex.RESIZE));
                        p.box(40, 30, "inherit");
                        p.box(40, 20, "top", h -> h.withVAlign(Align.START));
                    });
        }

        private void addFlowCells() {
            cell("Flow, start",
                    "flow().padding(4).spacing(3)",
                    WidgetLayout.flow().padding(4).spacing(3).build(),
                    LayoutTestScreen::flowBoxes);

            cell("Flow, centred",
                    "flow().lineAlign(CENTER).itemAlign(CENTER)",
                    WidgetLayout.flow().padding(4).spacing(3).lineAlign(Align.CENTER).itemAlign(Align.CENTER).build(),
                    LayoutTestScreen::flowBoxes);

            cell("Flow, end",
                    "flow().lineAlign(END).itemAlign(END)",
                    WidgetLayout.flow().padding(4).spacing(3).lineAlign(Align.END).itemAlign(Align.END).build(),
                    LayoutTestScreen::flowBoxes);
        }

        private void addGridCells() {
            cell("Form: auto + weight",
                    "columns(auto(), weight(1)).cellAlign(START, CENTER)",
                    WidgetLayout.grid().columns(Track.auto(), Track.weight(1)).padding(4).spacing(4, 3)
                            .cellAlign(Align.START, Align.CENTER).build(),
                    p -> {
                        String[] labels = {"Name", "Colour", "Size"};
                        for (String label : labels) {
                            p.box(40, 12, label);
                            p.box(10, 12, "value", h -> h.withHFlex(Flex.RESIZE));
                        }
                    });

            cell("Spans",
                    "span(3,1) header, span(1,2) sidebar, RESIZE cells",
                    WidgetLayout.grid().columns(Track.weight(1), Track.weight(1), Track.weight(1))
                            .rows(Track.fixed(16), Track.weight(1), Track.weight(1)).padding(4).spacing(3).build(),
                    p -> {
                        p.box(10, 10, "header", h -> h.resize().withSpan(3, 1));
                        p.box(10, 10, "side", h -> h.resize().withSpan(1, 2));
                        for (int i = 1; i <= 4; i++) {
                            p.box(10, 10, String.valueOf(i), h -> h.resize());
                        }
                    });

            cell("Fixed and weighted tracks",
                    "columns(fixed(40), weight(1), weight(2))",
                    WidgetLayout.grid().columns(Track.fixed(40), Track.weight(1), Track.weight(2))
                            .rows(Track.fixed(18)).padding(4).spacing(3).build(),
                    p -> {
                        for (int i = 1; i <= 6; i++) {
                            p.box(10, 14, "c" + i, h -> h.withHFlex(Flex.RESIZE));
                        }
                    });

            cell("Cell alignment",
                    "cellAlign(CENTER, CENTER); hints override",
                    WidgetLayout.grid().columns(Track.weight(1), Track.weight(1), Track.weight(1))
                            .rows(Track.weight(1), Track.weight(1)).padding(4).spacing(3)
                            .cellAlign(Align.CENTER, Align.CENTER).build(),
                    p -> {
                        for (int i = 1; i <= 4; i++) {
                            p.box(26, 14, "c" + i);
                        }
                        p.box(26, 14, "end", h -> h.withHAlign(Align.END).withVAlign(Align.END));
                        p.box(26, 14, "start", h -> h.withHAlign(Align.START).withVAlign(Align.START));
                    });
        }
    }

    private static void flowBoxes(DemoPanel p) {
        for (int i = 0; i < FLOW_WIDTHS.length; i++) {
            p.box(FLOW_WIDTHS[i], FLOW_HEIGHTS[i], String.valueOf(i + 1));
        }
    }
}
