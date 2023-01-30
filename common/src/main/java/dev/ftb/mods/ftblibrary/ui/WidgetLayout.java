package dev.ftb.mods.ftblibrary.ui;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author LatvianModder
 */
@FunctionalInterface
public interface WidgetLayout {
	Padding NO_PADDING = new Padding(0, 0);

	int align(Panel panel);

	/**
	 * This is called after {@link #align(Panel)} to get any extra padding that may have been allocated.
	 *
	 * @return any horizontal and vertical padding that might be needed
	 */
	@NotNull
	default Padding getLayoutPadding() {
		return NO_PADDING;
	}

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
					i += sizeGetter.apply(widget);
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
	}

	WidgetLayout NONE = panel -> 0;

	WidgetLayout VERTICAL = new Vertical(0, 0, 0);

	WidgetLayout HORIZONTAL = new Horizontal(0, 0, 0);

	record Padding(int vertical, int horizontal) {
	}
}