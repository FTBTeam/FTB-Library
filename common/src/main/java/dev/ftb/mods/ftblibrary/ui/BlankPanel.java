package dev.ftb.mods.ftblibrary.ui;

/**
 * @author LatvianModder
 */
public class BlankPanel extends Panel {
	public String id;

	public BlankPanel(Panel panel) {
		this(panel, "");
	}

	public BlankPanel(Panel panel, String _id) {
		super(panel);
		id = _id;
	}

	@Override
	public void clearWidgets() {
	}

	@Override
	public void addWidgets() {
	}

	@Override
	public void alignWidgets() {
	}

	@Override
	public String toString() {
		return id.isEmpty() ? super.toString() : id;
	}
}