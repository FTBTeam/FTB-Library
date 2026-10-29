package dev.ftb.mods.ftblibrary.client.gui.widget;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WidgetDrawLayerTest {
    private static Panel parent(boolean clip) {
        Panel panel = new Panel(null) {
            @Override
            public int getX() {
                return 0;
            }

            @Override
            public int getY() {
                return 0;
            }

            @Override
            public void addWidgets() {
            }

            @Override
            public void alignWidgets() {
            }
        };
        panel.setOnlyRenderWidgetsInside(clip);
        return panel;
    }

    @Test
    void rendersInOverriddenLayerWithoutSettingBackingField() {
        Widget widget = new Widget(parent(false)) {
            @Override
            public DrawLayer getDrawLayer() {
                return DrawLayer.BACKGROUND;
            }
        };

        assertTrue(widget.shouldRenderInLayer(Widget.DrawLayer.BACKGROUND, 0, 0, 100, 100));
        assertFalse(widget.shouldRenderInLayer(Widget.DrawLayer.FOREGROUND, 0, 0, 100, 100));
    }

    @Test
    void preservesDefaultAndExplicitLayers() {
        Widget widget = new Widget(parent(false));
        assertTrue(widget.shouldRenderInLayer(Widget.DrawLayer.FOREGROUND, 0, 0, 100, 100));
        assertFalse(widget.shouldRenderInLayer(Widget.DrawLayer.BACKGROUND, 0, 0, 100, 100));

        widget.setDrawLayer(Widget.DrawLayer.BACKGROUND);
        assertTrue(widget.shouldRenderInLayer(Widget.DrawLayer.BACKGROUND, 0, 0, 100, 100));
        assertFalse(widget.shouldRenderInLayer(Widget.DrawLayer.FOREGROUND, 0, 0, 100, 100));
    }

    @Test
    void preservesHiddenWidgetCheck() {
        Widget widget = new Widget(parent(false)) {
            @Override
            public boolean shouldDraw() {
                return false;
            }
        };
        assertFalse(widget.shouldRenderInLayer(Widget.DrawLayer.FOREGROUND, 0, 0, 100, 100));
    }

    @Test
    void preservesClippingAndItsOptOut() {
        Panel panel = parent(true);
        Widget widget = new Widget(panel);
        widget.setPosAndSize(200, 200, 10, 10);
        assertFalse(widget.shouldRenderInLayer(Widget.DrawLayer.FOREGROUND, 0, 0, 100, 100));

        widget.setPosAndSize(10, 10, 10, 10);
        assertTrue(widget.shouldRenderInLayer(Widget.DrawLayer.FOREGROUND, 0, 0, 100, 100));

        widget.setPosAndSize(200, 200, 10, 10);
        panel.setOnlyRenderWidgetsInside(false);
        assertTrue(widget.shouldRenderInLayer(Widget.DrawLayer.FOREGROUND, 0, 0, 100, 100));
    }
}
