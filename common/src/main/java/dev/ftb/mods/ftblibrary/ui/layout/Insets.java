package dev.ftb.mods.ftblibrary.ui.layout;

/**
 * Space reserved around the content of a panel, in pixels.
 */
public record Insets(int top, int right, int bottom, int left) {
    public static final Insets ZERO = new Insets(0, 0, 0, 0);

    /** Same inset on all four sides. */
    public static Insets of(int all) {
        return new Insets(all, all, all, all);
    }

    /** One value for top and bottom, one for left and right. */
    public static Insets of(int vertical, int horizontal) {
        return new Insets(vertical, horizontal, vertical, horizontal);
    }

    /** Total of top and bottom. */
    public int vertical() {
        return top + bottom;
    }

    /** Total of left and right. */
    public int horizontal() {
        return left + right;
    }
}
