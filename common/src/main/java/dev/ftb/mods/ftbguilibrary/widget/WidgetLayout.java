package dev.ftb.mods.ftbguilibrary.widget;

/**
 * @author LatvianModder
 */
@FunctionalInterface
public interface WidgetLayout {
	int align(Panel panel);

	class Vertical implements WidgetLayout {
		private final int pre, spacing, post;

		public Vertical(int _pre, int _spacing, int _post) {
			pre = _pre;
			spacing = _spacing;
			post = _post;
		}

		@Override
		public int align(Panel panel) {
			int i = pre;

			if (!panel.widgets.isEmpty()) {
				for (Widget widget : panel.widgets) {
					widget.setY(i);
					i += widget.height + spacing;
				}

				i -= spacing;
			}

			return i + post;
		}
	}

	class Horizontal implements WidgetLayout {
		private final int pre, spacing, post;

		public Horizontal(int _pre, int _spacing, int _post) {
			pre = _pre;
			spacing = _spacing;
			post = _post;
		}

		@Override
		public int align(Panel panel) {
			int i = pre;

			if (!panel.widgets.isEmpty()) {
				for (Widget widget : panel.widgets) {
					widget.setX(i);
					i += widget.width + spacing;
				}

				i -= spacing;
			}

			return i + post;
		}
	}

	WidgetLayout NONE = panel -> 0;

	WidgetLayout VERTICAL = new Vertical(0, 0, 0);

	WidgetLayout HORIZONTAL = new Horizontal(0, 0, 0);
}