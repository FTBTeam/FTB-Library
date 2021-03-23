package dev.ftb.mods.ftbguilibrary.widget;

/**
 * @author LatvianModder
 */
public class PanelScrollBar extends ScrollBar {
	public final Panel panel;

	public PanelScrollBar(Panel parent, Plane plane, Panel p) {
		super(parent, plane, 0);
		panel = p;
		panel.attachedScrollbar = this;
	}

	public PanelScrollBar(Panel parent, Panel panel) {
		this(parent, Plane.VERTICAL, panel);
	}

	@Override
	public void setMinValue(double min) {
	}

	@Override
	public double getMinValue() {
		return 0;
	}

	@Override
	public void setMaxValue(double max) {
		super.setMaxValue(max - (plane.isVertical ? panel.height : panel.width));
	}

	@Override
	public void setScrollStep(double s) {
		panel.setScrollStep(s);
	}

	@Override
	public double getScrollStep() {
		return panel.getScrollStep();
	}

	@Override
	public int getScrollBarSize() {
		double max = getMaxValue();

		if (max <= 0) {
			return 0;
		}

		int size;

		if (plane.isVertical) {
			size = (int) (panel.height / (max + panel.height) * height);
		} else {
			size = (int) (panel.width / (max + panel.width) * width);
		}

		return Math.max(size, 10);
	}

	@Override
	public void onMoved() {
		double value = getMaxValue() <= 0 ? 0 : getValue();

		if (plane.isVertical) {
			panel.setScrollY(value);
		} else {
			panel.setScrollX(value);
		}
	}

	@Override
	public boolean canMouseScroll() {
		return super.canMouseScroll() || panel.isMouseOver();
	}
}