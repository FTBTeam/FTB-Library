package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.ui.layout.Flex;
import dev.ftb.mods.ftblibrary.ui.layout.GridLayout.Track;
import dev.ftb.mods.ftblibrary.ui.layout.LayoutHints;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** A screen is initialised again when the window changes size, keeping its own panels but not their contents. */
class ReinitLayoutTest {
    private static class Content extends Panel {
        Content(Panel parent) {
            super(parent);
        }

        @Override
        protected @Nullable WidgetLayout getLayout() {
            return WidgetLayout.grid().columns(Track.weight(1), Track.weight(1)).rows(Track.weight(1)).spacing(4).build();
        }

        @Override
        public void addWidgets() {
            for (int i = 0; i < 2; i++) {
                Panel cell = new Panel(this) {
                    @Override
                    public void addWidgets() {
                        Widget w = new Widget(this);
                        w.setSize(10, 10);
                        w.setLayoutHints(LayoutHints.DEFAULT.resize());
                        add(w);
                    }

                    @Override
                    protected WidgetLayout getLayout() {
                        return WidgetLayout.vertical().fillCross().build();
                    }
                };
                cell.setLayoutHints(LayoutHints.DEFAULT.resize());
                add(cell);
            }
        }
    }

    private static class Screen extends BaseScreen {
        final Widget bar = new Widget(this);
        final Content content = new Content(this);

        Screen() {
            super(null);
            bar.setSize(10, 20);
            content.setLayoutHints(LayoutHints.DEFAULT.withVFlex(Flex.RESIZE));
        }

        @Override
        public void addWidgets() {
            add(bar);
            add(content);
        }

        @Override
        protected @Nullable WidgetLayout getLayout() {
            return WidgetLayout.vertical().padding(4).spacing(4).fillCross().build();
        }
    }

    private static void check(Screen screen, int w, int h) {
        assertEquals(w - 8, screen.content.width);
        assertEquals(h - 8 - 20 - 4, screen.content.height);
        int cellW = (w - 8 - 4) / 2;
        for (Widget cell : screen.content.getWidgets()) {
            assertEquals(h - 8 - 20 - 4, cell.height, "cell height at " + w + "x" + h);
            assertEquals(cellW, ((Panel) cell).getWidgets().getFirst().width, "inner width at " + w + "x" + h);
        }
    }

    @Test
    void reinitialisingAtANewSizeRelaysEverythingOut() {
        Screen screen = new Screen();
        screen.setSize(400, 300);
        screen.initGui();
        check(screen, 400, 300);

        screen.setSize(800, 500);
        screen.initGui();
        check(screen, 800, 500);

        screen.setSize(200, 150);
        screen.initGui();
        check(screen, 200, 150);
    }
}
