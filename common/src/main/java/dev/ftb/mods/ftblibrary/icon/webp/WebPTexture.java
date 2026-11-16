package dev.ftb.mods.ftblibrary.icon.webp;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;

/**
 * A GPU texture holding one WebP frame at a time. Must be created, uploaded to and closed on the render thread.
 */
public class WebPTexture extends DynamicTexture {
    private final int width;
    private final int height;

    public WebPTexture(int width, int height) {
        super(new NativeImage(NativeImage.Format.RGBA, width, height, false));
        this.width = width;
        this.height = height;
    }

    /**
     * Upload a frame into this texture.
     *
     * @param argb canvas-sized pixels as 0xAARRGGBB
     */
    public void uploadFrame(int[] argb) {
        NativeImage image = getPixels();
        if (image == null || argb.length < width * height) {
            return;
        }
        for (int y = 0; y < height; y++) {
            int row = y * width;
            for (int x = 0; x < width; x++) {
                int p = argb[row + x];
                // NativeImage RGBA format stores 0xAABBGGRR
                image.setPixelRGBA(x, y, (p & 0xFF00FF00) | ((p >> 16) & 0xFF) | ((p & 0xFF) << 16));
            }
        }
        upload();
    }
}
