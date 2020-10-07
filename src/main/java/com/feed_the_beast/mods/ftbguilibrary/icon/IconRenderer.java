package com.feed_the_beast.mods.ftbguilibrary.icon;

import com.feed_the_beast.mods.ftbguilibrary.FTBGUILibraryClient;
import com.feed_the_beast.mods.ftbguilibrary.utils.IPixelBuffer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author LatvianModder
 * @author elytra
 * @see <a href='https://github.com/elytra/BlockRenderer'>elytra/BlockRenderer</a>
 */
public class IconRenderer
{
	private static class IconCallbackPair implements Runnable
	{
		private Icon icon;
		private ImageCallback callback;

		@Override
		public void run()
		{
			QUEUE.add(this);
			FTBGUILibraryClient.shouldRenderIcons = true;
		}
	}

	private static final Collection<IconCallbackPair> QUEUE = new LinkedList<>();
	private static Image nullImage = null;
	private static Map<Icon, Image> imageCache = new HashMap<>();

	public static void clearCache()
	{
		imageCache = new HashMap<>();
	}

	public static Image getNullImage()
	{
		if (nullImage == null)
		{
			ResourceLocation rl;

			if (ModList.get().isLoaded("missingitem"))
			{
				rl = new ResourceLocation("missingitem:textures/items/missingitem.png");
			}
			else
			{
				rl = new ResourceLocation("ftbguilibrary:textures/icons/cancel.png");
			}

			try (InputStream stream = Minecraft.getInstance().getResourceManager().getResource(rl).getInputStream())
			{
				nullImage = new Image(stream);
			}
			catch (Exception ex)
			{
			}
		}

		return nullImage;
	}

	public static boolean load(@Nullable Icon icon, ImageCallback callback)
	{
		if (icon == null)
		{
			callback.imageLoaded(false, null);
			return true;
		}
		else if (icon.isEmpty())
		{
			callback.imageLoaded(false, getNullImage());
			return true;
		}

		Image image = imageCache.get(icon);

		if (image != null)
		{
			callback.imageLoaded(false, image);
			return true;
		}

		if (icon.hasPixelBuffer())
		{
			IPixelBuffer buffer = icon.createPixelBuffer();

			if (buffer == null)
			{
				image = getNullImage();
			}
			else
			{
				int w = buffer.getWidth();
				int h = buffer.getHeight();
				image = new WritableImage(w, h);
				((WritableImage) image).getPixelWriter().setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(), buffer.getPixels(), 0, w);
			}

			imageCache.put(icon, image);
			callback.imageLoaded(false, image);
			return true;
		}

		imageCache.put(icon, getNullImage());
		callback.imageLoaded(false, getNullImage());

		IconCallbackPair pair = new IconCallbackPair();
		pair.icon = icon;
		pair.callback = callback;
		Minecraft.getInstance().enqueue(pair);

		return false;
	}

	/**
	 * Modified version of BlockRenderer mod code
	 */
	public static void render()
	{
		if (QUEUE.isEmpty())
		{
			return;
		}

		IconCallbackPair[] queued = QUEUE.toArray(new IconCallbackPair[0]);
		QUEUE.clear();

		Minecraft mc = Minecraft.getInstance();
		MainWindow res = mc.getMainWindow();
		int size = Math.min(Math.min(res.getWidth(), res.getHeight()), 64);

		//FIXME: Translate and scale the matrix?
		MatrixStack matrixStack = new MatrixStack();
		//FIXME: Check if its needed, used to be mc.entityRenderer.setupOverlayRendering();
		//res.loadGUIRenderMatrix(Minecraft.IS_RUNNING_ON_MAC);
		RenderHelper.enableStandardItemLighting();
		double scale = size / (16D * res.getGuiScaleFactor());
		RenderSystem.translated(0, 0, -(scale * 100D));
		RenderSystem.scaled(scale, scale, scale);

		float oldZLevel = mc.getItemRenderer().zLevel;
		mc.getItemRenderer().zLevel = -50;

		RenderSystem.enableRescaleNormal();
		RenderSystem.enableColorMaterial();
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		RenderSystem.disableAlphaTest();

		int[] pixels = new int[size * size];
		AffineTransform at = new AffineTransform();
		at.concatenate(AffineTransform.getScaleInstance(1, -1));
		at.concatenate(AffineTransform.getTranslateInstance(0, -size));
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

		for (IconCallbackPair pair : queued)
		{
			RenderSystem.pushMatrix();
			RenderSystem.clearColor(0F, 0F, 0F, 0F);
			RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, false);
			pair.icon.drawStatic(matrixStack, 0, 0, 16, 16);
			RenderSystem.popMatrix();

			try
			{
				ByteBuffer buf = BufferUtils.createByteBuffer(size * size * 4);
				GL11.glReadBuffer(GL11.GL_BACK);
				GL11.glGetError();
				//RenderSystem.getError(); //FIXME: For some reason it throws error here, but it still works. Calling this to not spam console
				GL11.glReadPixels(0, res.getHeight() - size, size, size, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, buf);
				buf.asIntBuffer().get(pixels);
				img.setRGB(0, 0, size, size, pixels, 0, size);
				BufferedImage flipped = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = flipped.createGraphics();
				g.transform(at);
				g.drawImage(img, 0, 0, null);
				g.dispose();
				pixels = flipped.getRGB(0, 0, size, size, pixels, 0, size);
				WritableImage image = new WritableImage(size, size);
				image.getPixelWriter().setPixels(0, 0, size, size, PixelFormat.getIntArgbInstance(), pixels, 0, size);
				imageCache.put(pair.icon, image);
				pair.callback.imageLoaded(true, image);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		RenderSystem.disableLighting();
		RenderSystem.disableColorMaterial();
		RenderSystem.disableDepthTest();
		RenderSystem.disableBlend();
		mc.getItemRenderer().zLevel = oldZLevel;
	}
}