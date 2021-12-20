package dev.ftb.mods.ftblibrary.ui.input;

/**
 * @author LatvianModder
 */
public class MouseButton {
	private static final MouseButton[] BUTTONS = new MouseButton[16];

	static {
		for (var i = 0; i < BUTTONS.length; i++) {
			BUTTONS[i] = new MouseButton(i);
		}
	}

	public static MouseButton get(int i) {
		return i >= 0 && i < BUTTONS.length ? BUTTONS[i] : BUTTONS[BUTTONS.length - 1];
	}

	public static final MouseButton LEFT = get(0);
	public static final MouseButton RIGHT = get(1);
	public static final MouseButton MIDDLE = get(2);
	public static final MouseButton BACK = get(3);
	public static final MouseButton NEXT = get(4);

	public final int id;

	private MouseButton(int b) {
		id = b;
	}

	public int hashCode() {
		return id;
	}

	public boolean isLeft() {
		return id == LEFT.id;
	}

	public boolean isRight() {
		return id == RIGHT.id;
	}

	public boolean isMiddle() {
		return id == MIDDLE.id;
	}

	public int getId() {
		return id;
	}
}