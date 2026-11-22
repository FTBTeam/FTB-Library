package dev.ftb.mods.ftblibrary.ui.layout;

import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftblibrary.ui.WidgetLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Lays all of a panel's widgets over the same area, each one anchored by its own {@link LayoutHints}. This covers
 * "fill the panel", "centre in the panel" and "pin to the bottom-right corner" without computing coordinates by hand:
 * <pre>{@code
 * closeButton.setLayoutHints(LayoutHints.DEFAULT.withHAlign(Align.END).withMargin(Insets.of(4)));
 * background.setLayoutHints(LayoutHints.DEFAULT.resize());
 * align(WidgetLayout.stack().build());
 * }</pre>
 * <p>
 * For each axis a widget is either {@link Flex#RESIZE} (stretched to the area, less its margins, then clamped to its
 * min/max size) or keeps its size and is placed by its alignment hint, falling back to the layout's default alignment.
 * The area is the panel's size minus the insets, or the largest widget (with margins) if the panel has no size yet.
 * <p>
 * {@link #align(Panel)} returns the height needed: the tallest widget with its margins, plus vertical insets.
 */
public final class StackLayout implements WidgetLayout {
    private final Insets insets;
    private final Align hAlign;
    private final Align vAlign;

    private StackLayout(Insets insets, Align hAlign, Align vAlign) {
        this.insets = insets;
        this.hAlign = hAlign;
        this.vAlign = vAlign;
    }

    @Override
    public int align(Panel panel) {
        return align(panel, LayoutMath.available(panel.getWidth()), LayoutMath.available(panel.getHeight()));
    }

    @Override
    public int align(Panel panel, int availableWidth, int availableHeight) {
        List<Widget> widgets = panel.getWidgets();

        Size largest = largestFootprint(widgets);
        int widest = largest.width();
        int tallest = largest.height();

        int areaW = availableWidth >= 0 ? Math.max(0, availableWidth - insets.horizontal()) : widest;
        int areaH = availableHeight >= 0 ? Math.max(0, availableHeight - insets.vertical()) : tallest;

        for (Widget w : widgets) {
            LayoutHints h = w.getLayoutHints();
            Insets m = h.margin();

            if (h.hFlex() == Flex.RESIZE && !w.isFittingContent(true)) {
                w.setWidth(Math.clamp(areaW - m.horizontal(), h.minWidth(), h.maxWidth()));
            }
            if (h.vFlex() == Flex.RESIZE && !w.isFittingContent(false)) {
                w.setHeight(Math.clamp(areaH - m.vertical(), h.minHeight(), h.maxHeight()));
            }

            Align ha = h.hAlign() != null ? h.hAlign() : hAlign;
            Align va = h.vAlign() != null ? h.vAlign() : vAlign;
            w.setX(insets.left() + m.left() + LayoutMath.alignOffset(ha, areaW - m.horizontal(), w.getWidth()));
            w.setY(insets.top() + m.top() + LayoutMath.alignOffset(va, areaH - m.vertical(), w.getHeight()));
        }

        return tallest + insets.vertical();
    }

    /**
     * The size needed to hold the largest widget, with its margins, plus the insets. Widgets sit on top of each other,
     * so this does not depend on how many there are. The available size is ignored.
     */
    @Override
    public Size measure(Panel panel, int availableWidth, int availableHeight) {
        Size largest = largestFootprint(panel.getWidgets());
        return new Size(largest.width() + insets.horizontal(), largest.height() + insets.vertical());
    }

    private static Size largestFootprint(List<Widget> widgets) {
        int widest = 0;
        int tallest = 0;
        for (Widget w : widgets) {
            Insets m = w.getLayoutHints().margin();
            widest = Math.max(widest, w.getWidth() + m.horizontal());
            tallest = Math.max(tallest, w.getHeight() + m.vertical());
        }
        return new Size(widest, tallest);
    }

    @Override
    @NotNull
    public Padding getLayoutPadding() {
        return new Padding(insets.vertical(), insets.horizontal());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Insets insets = Insets.ZERO;
        private Align hAlign = Align.START;
        private Align vAlign = Align.START;

        private Builder() {
        }

        /** Space kept clear around the area the widgets are placed in. */
        public Builder padding(Insets insets) {
            this.insets = Objects.requireNonNull(insets);
            return this;
        }

        public Builder padding(int all) {
            return padding(Insets.of(all));
        }

        /** Where widgets without their own alignment hint sit within the area. */
        public Builder align(Align horizontal, Align vertical) {
            this.hAlign = Objects.requireNonNull(horizontal);
            this.vAlign = Objects.requireNonNull(vertical);
            return this;
        }

        public StackLayout build() {
            return new StackLayout(insets, hAlign, vAlign);
        }
    }
}
