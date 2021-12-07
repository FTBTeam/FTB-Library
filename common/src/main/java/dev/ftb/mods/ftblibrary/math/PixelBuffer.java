package dev.ftb.mods.ftblibrary.math;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class PixelBuffer {
	public static PixelBuffer from(BufferedImage img) {
		PixelBuffer buffer = new PixelBuffer(img.getWidth(), img.getHeight());
		buffer.setPixels(img.getRGB(0, 0, buffer.getWidth(), buffer.getHeight(), buffer.getPixels(), 0, buffer.getWidth()));
		return buffer;
	}

	public static PixelBuffer from(InputStream stream) throws Exception {
		return from(ImageIO.read(stream));
	}

	private final int width, height;
	private final int[] pixels;

	public PixelBuffer(int w, int h) {
		width = w;
		height = h;
		pixels = new int[w * h];
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int[] getPixels() {
		return pixels;
	}

	public void setPixels(int[] p) {
		if (p.length == pixels.length) {
			System.arraycopy(p, 0, pixels, 0, pixels.length);
		}
	}

	public void setRGB(int x, int y, int col) {
		pixels[x + y * width] = col;
	}

	public int getRGB(int x, int y) {
		return pixels[x + y * width];
	}

	public void setRGB(int startX, int startY, int w, int h, int[] rgbArray) {
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

	public void setRGB(int startX, int startY, PixelBuffer buffer) {
		setRGB(startX, startY, buffer.getWidth(), buffer.getHeight(), buffer.getPixels());
	}

	public int[] getRGB(int startX, int startY, int w, int h, @Nullable int[] p) {
		if (p == null || p.length != w * h) {
			p = new int[w * h];
		}

		int off = -1;
		w += startX;
		h += startY;
		for (int y = startY; y < h; y++) {
			for (int x = startX; x < w; x++) {
				p[++off] = getRGB(x, y);
			}
		}

		return p;
	}

	public void fill(int col) {
		int[] pixels = getPixels();
		Arrays.fill(pixels, col);
		setPixels(pixels);
	}

	public void fill(int startX, int startY, int w, int h, int col) {
		for (int y = startY; y < startY + h; y++) {
			for (int x = startX; x < startX + w; x++) {
				setRGB(x, y, col);
			}
		}
	}

	public BufferedImage toImage(int type) {
		BufferedImage image = new BufferedImage(width, height, type);
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else if (o == this) {
			return true;
		} else if (o instanceof PixelBuffer b) {
			if (width == b.width && height == b.height) {
				for (int i = 0; i < pixels.length; i++) {
					if (pixels[i] != b.pixels[i]) {
						return false;
					}
				}

				return true;
			}
		}
		return false;
	}

	public int hashCode() {
		return Arrays.hashCode(getPixels());
	}

	public PixelBuffer copy() {
		PixelBuffer b = new PixelBuffer(width, height);
		System.arraycopy(pixels, 0, b.pixels, 0, pixels.length);
		return b;
	}

	public PixelBuffer getSubimage(int x, int y, int w, int h) {
		PixelBuffer b = new PixelBuffer(w, h);
		getRGB(x, y, w, h, b.pixels);
		return b;
	}

	public ByteBuffer toByteBuffer(boolean alpha) {
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