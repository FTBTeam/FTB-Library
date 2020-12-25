package com.feed_the_beast.mods.ftbguilibrary.utils;

import org.jetbrains.annotations.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Arrays;

public class PixelBuffer implements IPixelBuffer {
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

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int[] getPixels() {
        return pixels;
    }

    @Override
    public void setPixels(int[] p) {
        if (p.length == pixels.length) {
            System.arraycopy(p, 0, pixels, 0, pixels.length);
        }
    }

    @Override
    public void setRGB(int x, int y, int col) {
        pixels[x + y * width] = col;
    }

    @Override
    public int getRGB(int x, int y) {
        return pixels[x + y * width];
    }

    @Override
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
        } else if (o instanceof PixelBuffer) {
            PixelBuffer b = (PixelBuffer) o;
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

    @Override
    public PixelBuffer copy() {
        PixelBuffer b = new PixelBuffer(width, height);
        System.arraycopy(pixels, 0, b.pixels, 0, pixels.length);
        return b;
    }

    @Override
    public PixelBuffer getSubimage(int x, int y, int w, int h) {
        PixelBuffer b = new PixelBuffer(w, h);
        getRGB(x, y, w, h, b.pixels);
        return b;
    }
}