package dev.ftb.mods.ftblibrary.ui;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The managed layouts behind {@link ContextMenu} and {@link DropDownMenu}. The menus themselves need a running
 * client, so these use plain widgets in the same arrangement.
 */
class PopupMenuLayoutTest {
    private static final class Managed extends Panel {
        private final WidgetLayout layout;

        Managed(WidgetLayout layout) {
            super(null);
            this.layout = layout;
            setFitContent(true, true);
        }

        @Override
        public void addWidgets() {
        }

        @Override
        protected @Nullable WidgetLayout getLayout() {
            return layout;
        }
    }

    private static Widget add(Panel panel, int w, int h) {
        Widget widget = new Widget(panel);
        widget.setSize(w, h);
        panel.add(widget);
        return widget;
    }

    @Test
    void contextMenuColumnMatchesTheHandPlacedOne() {
        Managed menu = new Managed(ContextMenu.SINGLE_COLUMN);
        Widget a = add(menu, 40, 12);
        Widget b = add(menu, 70, 12);
        Widget c = add(menu, 55, 5);

        menu.alignWidgets();

        // old code: 3 margin all round, one pixel between items, every item as wide as the widest
        assertEquals(70 + 6, menu.width);
        assertEquals(3 + 12 + 1 + 12 + 1 + 5 + 3, menu.height);
        for (Widget w : new Widget[]{a, b, c}) {
            assertEquals(3, w.posX);
            assertEquals(70, w.width);
        }
        assertEquals(3, a.posY);
        assertEquals(3 + 12 + 1, b.posY);
        assertEquals(3 + 12 + 1 + 12 + 1, c.posY);
    }

    @Test
    void dropDownPlacesSearchBoxListAndScrollbar() {
        Managed menu = new Managed(DropDownMenu.layout());
        Widget search = new Widget(menu);
        Widget list = add(menu, 128, 74);
        Widget bar = new Widget(menu);
        menu.add(search);
        menu.add(bar);
        DropDownMenu.describeParts(search, list, bar);

        menu.alignWidgets();

        assertEquals(128 + 14, menu.width);
        assertEquals(14 + 74, menu.height);

        assertEquals(0, search.posX);
        assertEquals(0, search.posY);
        assertEquals(142, search.width);
        assertEquals(12, search.height);

        assertEquals(0, list.posX);
        assertEquals(14, list.posY);

        assertEquals(128, bar.posX);
        assertEquals(12, bar.posY);
        assertEquals(14, bar.width);
        assertEquals(88 - 12, bar.height);
    }
}
