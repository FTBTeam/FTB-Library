package dev.ftb.mods.ftbguilibrary.widget;

/**
 * @author LatvianModder
 */
public class VerticalSpaceWidget extends Widget {
	public VerticalSpaceWidget(Panel p, int h) {
		super(p);
		setSize(1, h);
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public boolean shouldDraw() {
		return false;
	}
}