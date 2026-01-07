package dev.ftb.mods.ftblibrary.client.icon;

import dev.ftb.mods.ftblibrary.icon.ImageIcon;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.TiledBlitRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jspecify.annotations.Nullable;

import javax.imageio.ImageIO;

public enum ImageIconRenderer implements IconRenderer<ImageIcon> {
    INSTANCE;

    @Override
    public void render(ImageIcon icon, GuiGraphics graphics, int x, int y, int w, int h) {
        // No tiling? Just do a normal blit
        if (icon.tileSize <= 0D) {
            graphics.blit(icon.texture, x, y, x + w, y + h, icon.minU, icon.maxU, icon.minV, icon.maxV);
            return;
        }

        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(icon.texture);

        graphics.guiRenderState.submitGuiElement(new TiledBlitRenderState(
                RenderPipelines.GUI_TEXTURED,
                TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler()),
                graphics.pose(),
                (int) icon.tileSize,
                (int) icon.tileSize,
                x,
                y,
                x + w,
                y + h,
                icon.minU,
                icon.maxU,
                icon.minV,
                icon.maxV,
                icon.color.rgba(),
                graphics.scissorStack.peek()
        ));
    }

    @Override
    public boolean hasPixelBuffer(ImageIcon icon) {
        return true;
    }

    @Override
    public @Nullable PixelBuffer createPixelBuffer(ImageIcon icon) {
        if (icon.uri == null) {
            try {
                return PixelBuffer.from(Minecraft.getInstance().getResourceManager().getResource(icon.texture).orElseThrow().open());
            } catch (Exception ex) {
                return null;
            }
        } else {
            try (var stream = icon.uri.toURL().openConnection(Minecraft.getInstance().getProxy()).getInputStream()) {
                return PixelBuffer.from(ImageIO.read(stream));
            } catch (Exception ex) {
                return null;
            }
        }
    }

    @Override
    public double aspectRatio(ImageIcon icon) {
        if (icon.maxV == icon.minV) return 1.0;

        return (icon.maxU - icon.minU) / (icon.maxV - icon.minV);
    }
}
