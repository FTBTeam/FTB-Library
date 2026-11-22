package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.ui.layout.FlowLayout;
import dev.ftb.mods.ftblibrary.ui.layout.GridLayout;
import dev.ftb.mods.ftblibrary.ui.layout.LinearLayout;
import dev.ftb.mods.ftblibrary.ui.layout.Size;
import dev.ftb.mods.ftblibrary.ui.layout.StackLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;


@FunctionalInterface
public interface WidgetLayout {
    Padding NO_PADDING = new Padding(0, 0);
    WidgetLayout NONE = panel -> 0;
    WidgetLayout VERTICAL = new Vertical(0, 0, 0);
    WidgetLayout HORIZONTAL = new Horizontal(0, 0, 0);

    /**
     * Align the panel's widget according to this layout's policy.
     *
     * @param panel the panel to lay out
     * @return the new height or width of the panel, depending on alignment direction
     * @apiNote the return value is intended for use when manually laying out widgets, and is ignored for
     * fully-managed layouts (i.e. when {@link Panel#getLayout()} returns a non-null result)
     */
    int align(Panel panel);

    /**
     * Start building a column layout, optionally with cross-axis alignment and fill.
     *
     * @return a builder; call {@link LinearLayout.Builder#build()} to get the layout
     * @see LinearLayout
     */
    static LinearLayout.Builder vertical() {
        return LinearLayout.vertical();
    }

    /**
     * Start building a layout that places every widget over the same area, anchored by each widget's hints.
     *
     * @see StackLayout
     */
    static StackLayout.Builder stack() {
        return StackLayout.builder();
    }

    /**
     * Start building a layout that fills lines left to right and wraps to the next when full.
     *
     * @see FlowLayout
     */
    static FlowLayout.Builder flow() {
        return FlowLayout.builder();
    }

    /**
     * Start building a layout of columns and rows.
     *
     * @see GridLayout
     */
    static GridLayout.Builder grid() {
        return GridLayout.builder();
    }

    /**
     * Start building a row layout, optionally with cross-axis alignment and fill.
     *
     * @return a builder; call {@link LinearLayout.Builder#build()} to get the layout
     * @see LinearLayout
     */
    static LinearLayout.Builder horizontal() {
        return LinearLayout.horizontal();
    }

    /**
     * {@link #align(Panel) Align} the panel's widgets within an explicit amount of room, instead of the panel's own
     * size. This is what lets a panel that fits its content lay its widgets out without its previous size getting in
     * the way: pass {@link Size#UNBOUNDED} for an axis with no limit, and widgets that would stretch to fill spare room
     * on that axis keep their current size instead.
     * <p>
     * The room is for the whole panel, including any padding, just like the panel's width or height. A value of zero
     * is a real limit (no room at all), not "unknown".
     * <p>
     * Layouts that do not override this ignore the limits and behave as {@link #align(Panel)}, which is the default,
     * so existing implementations are unaffected.
     *
     * @param panel           the panel whose widgets are laid out
     * @param availableWidth  room available horizontally, or {@link Size#UNBOUNDED}
     * @param availableHeight room available vertically, or {@link Size#UNBOUNDED}
     * @return the same result as {@link #align(Panel)}
     */
    default int align(Panel panel, int availableWidth, int availableHeight) {
        return align(panel);
    }

    /**
     * Work out the size this layout would like the panel to be, in order to hold its widgets, without moving or
     * resizing anything. This is what lets a panel fit itself to its content.
     * <p>
     * The answer is what the content <em>wants</em>, so widgets that would stretch to fill spare room are counted at
     * their current size. The available width and height only matter to layouts whose answer depends on them (a
     * wrapping flow needs a width to know how many lines it has); most ignore them.
     * <p>
     * Layouts that cannot say return {@code null}, which is the default, so existing implementations are unaffected.
     *
     * @param panel           the panel whose widgets would be laid out
     * @param availableWidth  room available horizontally, or {@link Size#UNBOUNDED}
     * @param availableHeight room available vertically, or {@link Size#UNBOUNDED}
     * @return the wanted size including any padding, or {@code null} if this layout cannot measure
     */
    @Nullable
    default Size measure(Panel panel, int availableWidth, int availableHeight) {
        return null;
    }

    /**
     * {@link #measure(Panel, int, int) Measure} with no limit on either axis.
     */
    @Nullable
    default Size measure(Panel panel) {
        return measure(panel, Size.UNBOUNDED, Size.UNBOUNDED);
    }

    /**
     * This is called after {@link #align(Panel)} to get any extra padding that may have been allocated.
     *
     * @return any horizontal and vertical padding that might be needed
     */
    @NotNull
    default Padding getLayoutPadding() {
        return NO_PADDING;
    }

    /**
     * Legacy layouts. While not deprecated at this time, it is recommended to use {@link WidgetLayout#horizontal()} and
     * {@link WidgetLayout#vertical()} instead of {@link WidgetLayout#HORIZONTAL} and {@link WidgetLayout#VERTICAL},
     * respectively; {@link LinearLayout} provides far more flexibility.
     */
    abstract class _Simple implements WidgetLayout {
        protected final int pre;
        protected final int spacing;
        protected final int post;
        private final Function<Widget, Integer> sizeGetter;
        private final BiConsumer<Widget, Integer> positionSetter;
        protected int padding;

        public _Simple(int _pre, int _spacing, int _post, Function<Widget, Integer> sizeGetter, BiConsumer<Widget, Integer> positionSetter) {
            pre = _pre;
            spacing = _spacing;
            post = _post;
            this.sizeGetter = sizeGetter;
            this.positionSetter = positionSetter;
        }

        @Override
        public int align(Panel panel) {
            var i = pre;

            if (!panel.widgets.isEmpty()) {
                for (var widget : panel.widgets) {
                    positionSetter.accept(widget, i);
                    i += sizeGetter.apply(widget) + spacing;
                }

                i -= spacing;
            }

            padding = pre + post;
            return i + post;
        }
    }

    class Vertical extends _Simple {
        public Vertical(int _pre, int _spacing, int _post) {
            super(_pre, _spacing, _post, Widget::getHeight, Widget::setY);
        }

        @Override
        @NotNull
        public Padding getLayoutPadding() {
            return new Padding(padding, 0);
        }

        @Override
        @Nullable
        public Size measure(Panel panel, int availableWidth, int availableHeight) {
            int widest = 0;
            int total = 0;
            for (Widget widget : panel.getWidgets()) {
                widest = Math.max(widest, widget.getWidth());
                total += widget.getHeight();
            }
            int gaps = Math.max(0, panel.getWidgets().size() - 1) * spacing;
            return new Size(widest, pre + total + gaps + post);
        }
    }

    class Horizontal extends _Simple {
        public Horizontal(int _pre, int _spacing, int _post) {
            super(_pre, _spacing, _post, Widget::getWidth, Widget::setX);
        }

        @Override
        @NotNull
        public Padding getLayoutPadding() {
            return new Padding(0, padding);
        }

        @Override
        @Nullable
        public Size measure(Panel panel, int availableWidth, int availableHeight) {
            int tallest = 0;
            int total = 0;
            for (Widget widget : panel.getWidgets()) {
                tallest = Math.max(tallest, widget.getHeight());
                total += widget.getWidth();
            }
            int gaps = Math.max(0, panel.getWidgets().size() - 1) * spacing;
            return new Size(pre + total + gaps + post, tallest);
        }
    }

    record Padding(int vertical, int horizontal) {
    }
}
