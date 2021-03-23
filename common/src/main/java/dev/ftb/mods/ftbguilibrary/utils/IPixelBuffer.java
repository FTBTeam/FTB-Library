package dev.ftb.mods.ftbguilibrary.utils;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author LatvianModder
 */
public interface IPixelBuffer {
	int getWidth();

	int getHeight();

	int[] getPixels();

	void setPixels(int[] p);

	void setRGB(int x, int y, int col);

	int getRGB(int x, int y);

	default void setRGB(int startX, int startY, int w, int h, int[] rgbArray) {
		if (startX == 0 && startY == 0 && w == getWidth() && h == getHeight()) {
			setPixels(rgbArray);
			return;
		}

		int off = -1;
		for (int y = startY; y < startY + h; y++) {
			for (int x = startX; x < startX + w; x++) {
				setRGB(x, y, rgbArray[++off]);
			}
		}
	}

	default void setRGB(int startX, int startY, IPixelBuffer buffer) {
		setRGB(startX, startY, buffer.getWidth(), buffer.getHeight(), buffer.getPixels());
	}

	int[] getRGB(int startX, int startY, int w, int h, @Nullable int[] p);

	default void fill(int col) {
		int[] pixels = getPixels();
		Arrays.fill(pixels, col);
		setPixels(pixels);
	}

	default void fill(int startX, int startY, int w, int h, int col) {
		for (int y = startY; y < startY + h; y++) {
			for (int x = startX; x < startX + w; x++) {
				setRGB(x, y, col);
			}
		}
	}

	IPixelBuffer copy();

	IPixelBuffer getSubimage(int x, int y, int w, int h);

	default ByteBuffer toByteBuffer(boolean alpha) {
		int[] pixels = getPixels();
		ByteBuffer bb = BufferUtils.createByteBuffer(pixels.length * 4);
		byte alpha255 = (byte) 255;

		for (int c : pixels) {
			bb.put((byte) (c >> 16));
			bb.put((byte) (c >> 8));
			bb.put((byte) c);
			bb.put(alpha ? (byte) (c >> 24) : alpha255);
		}

		bb.flip();
		return bb;
	}
}