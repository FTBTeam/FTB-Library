package dev.ftb.mods.ftblibrary.ui;

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
			var i = pre;

			if (!panel.widgets.isEmpty()) {
				for (var widget : panel.widgets) {
					widget.setY(i);
					i += widget.height + spacing;
				}

				i -= spacing;
			}

			panel.contentHeightExtra = pre + post;
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
			var i = pre;

			if (!panel.widgets.isEmpty()) {
				for (var widget : panel.widgets) {
					widget.setX(i);
					i += widget.width + spacing;
				}

				i -= spacing;
			}

			panel.contentWidthExtra = pre + post;
			return i + post;
		}
	}

	WidgetLayout NONE = panel -> 0;

	WidgetLayout VERTICAL = new Vertical(0, 0, 0);

	WidgetLayout HORIZONTAL = new Horizontal(0, 0, 0);
}