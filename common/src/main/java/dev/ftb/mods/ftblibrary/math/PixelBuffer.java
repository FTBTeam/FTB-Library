package dev.ftb.mods.ftblibrary.math;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class PixelBuffer {
    private final int width, height;
    private final int[] pixels;

    public PixelBuffer(int w, int h) {
        width = w;
        height = h;
        pixels = new int[w * h];
    }

    public static PixelBuffer from(BufferedImage img) {
        var buffer = new PixelBuffer(img.getWidth(), img.getHeight());
        buffer.setPixels(img.getRGB(0, 0, buffer.getWidth(), buffer.getHeight(), buffer.getPixels(), 0, buffer.getWidth()));
        return buffer;
    }

    public static PixelBuffer from(InputStream stream) throws Exception {
        return from(ImageIO.read(stream));
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

        var off = -1;
        for (var y = startY; y < startY + h; y++) {
            for (var x = startX; x < startX + w; x++) {
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

        var off = -1;
        w += startX;
        h += startY;
        for (var y = startY; y < h; y++) {
            for (var x = startX; x < w; x++) {
                p[++off] = getRGB(x, y);
            }
        }

        return p;
    }

    public void fill(int col) {
        var pixels = getPixels();
        Arrays.fill(pixels, col);
        setPixels(pixels);
    }

    public void fill(int startX, int startY, int w, int h, int col) {
        for (var y = startY; y < startY + h; y++) {
            for (var x = startX; x < startX + w; x++) {
                setRGB(x, y, col);
            }
        }
    }

    public BufferedImage toImage(int type) {
        var image = new BufferedImage(width, height, type);
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
                for (var i = 0; i < pixels.length; i++) {
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
        var b = new PixelBuffer(width, height);
        System.arraycopy(pixels, 0, b.pixels, 0, pixels.length);
        return b;
    }

    public PixelBuffer getSubimage(int x, int y, int w, int h) {
        var b = new PixelBuffer(w, h);
        getRGB(x, y, w, h, b.pixels);
        return b;
    }

    public ByteBuffer toByteBuffer(boolean alpha) {
        var pixels = getPixels();
        var bb = BufferUtils.createByteBuffer(pixels.length * 4);
        var alpha255 = (byte) 255;

        for (var c : pixels) {
            bb.put((byte) (c >> 16));
            bb.put((byte) (c >> 8));
            bb.put((byte) c);
            bb.put(alpha ? (byte) (c >> 24) : alpha255);
        }

        bb.flip();
        return bb;
    }
}