package dev.ftb.mods.ftblibrary.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseScreenLayoutTest {
    private static class Screen extends BaseScreen {
        Screen() {
            super(null);
        }

        @Override
        public void addWidgets() {
            for (int h : new int[]{10, 20}) {
                Widget w = new Widget(this);
                w.setSize(30, h);
                add(w);
            }
        }
    }

    @Test
    void screenWithoutALayoutKeepsItsSize() {
        Screen screen = new Screen();
        screen.initGui();

        assertEquals(176, screen.width);
        assertEquals(166, screen.height);
        assertEquals(0, screen.getWidgets().getFirst().posY);
    }

    @Test
    void screenStillUsesItsOwnAlignWidgets() {
        Screen screen = new Screen() {
            @Override
            public void alignWidgets() {
                getWidgets().get(1).setY(77);
            }
        };
        screen.initGui();

        assertEquals(77, screen.getWidgets().get(1).posY);
    }

    @Test
    void screenWithALayoutLaysItselfOutAndFitsItsContent() {
        Screen screen = new Screen() {
            @Override
            protected WidgetLayout getLayout() {
                return WidgetLayout.vertical().padding(4).spacing(2).build();
            }
        };
        screen.setFitContent(true, true);
        screen.initGui();

        assertEquals(30 + 8, screen.width);
        assertEquals(4 + 10 + 2 + 20 + 4, screen.height);
        assertEquals(4, screen.getWidgets().get(0).posY);
        assertEquals(4 + 10 + 2, screen.getWidgets().get(1).posY);
    }
}
