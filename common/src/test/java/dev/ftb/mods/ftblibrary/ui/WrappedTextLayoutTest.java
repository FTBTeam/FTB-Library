package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.ui.layout.LayoutHints;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WrappedTextLayoutTest {
    private static final class Managed extends Panel {
        Managed(WidgetLayout layout, int width) {
            super(null);
            this.layout = layout;
            setWidth(width);
            setFitContent(false, true);
        }

        private final WidgetLayout layout;

        @Override
        public void addWidgets() {
        }

        @Override
        protected @Nullable WidgetLayout getLayout() {
            return layout;
        }
    }

    /** Every character is 6 wide and a line is 9 tall; breaks fall between words. */
    private static final class FakeText extends TextField {
        FakeText(Panel panel) {
            super(panel);
        }

        @Override
        FormattedText[] wrap(Component text, int width) {
            List<FormattedText> lines = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            for (String word : text.getString().split(" ")) {
                if (!line.isEmpty() && (line.length() + 1 + word.length()) * 6 > width) {
                    lines.add(FormattedText.of(line.toString()));
                    line.setLength(0);
                }
                line.append(!line.isEmpty() ? " " : "").append(word);
            }
            lines.add(FormattedText.of(line.toString()));
            return lines.toArray(new FormattedText[0]);
        }

        @Override
        int fontHeight() {
            return 9;
        }
    }

    @Test
    void textRewrapsAndThePanelFollows() {
        Managed panel = new Managed(WidgetLayout.vertical().fillCross().build(), 60);
        FakeText text = new FakeText(panel);
        text.setWrapToWidth();
        text.setLayoutHints(LayoutHints.DEFAULT.resize());
        text.setWidth(10);
        panel.add(text);
        Widget footer = new Widget(panel);
        footer.setSize(20, 5);
        panel.add(footer);

        text.setText("aaaa bbbb cccc dddd");
        panel.alignWidgets();

        // 60 wide takes ten characters a line: two lines of 10 - 2 = 18, then the footer
        assertEquals(60, text.width);
        assertEquals(18, text.height);
        assertEquals(18 + 5, panel.height);
        assertEquals(18, footer.posY);

        panel.setWidth(120);
        panel.validateLayout();

        assertEquals(120, text.width);
        assertEquals(8, text.height);
        assertEquals(8 + 5, panel.height);
        assertEquals(8, footer.posY);
    }

    @Test
    void textWithoutTheOptInKeepsItsWidth() {
        Managed panel = new Managed(WidgetLayout.vertical().fillCross().build(), 60);
        FakeText text = new FakeText(panel);
        text.setWidth(30);
        text.setHeight(10);
        panel.add(text);

        panel.alignWidgets();

        // stretched, but not rewrapped
        assertEquals(60, text.width);
        assertEquals(10, text.height);
    }
}
