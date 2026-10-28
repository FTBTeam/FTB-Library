package dev.ftb.mods.ftblibrary.icon.webp;

/**
 * A single fully-composited animation frame.
 *
 * @param argb       non-premultiplied pixels as 0xAARRGGBB, canvas sized; must not be mutated
 * @param durationMs how long this frame is displayed
 */
public record WebPFrameData(int[] argb, int durationMs) {
}
